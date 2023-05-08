package com.accountrix.banxi.service.plaid;

import com.accountrix.banxi.model.plaid.PlaidItemService;
import com.accountrix.banxi.model.plaid.item.PlaidItem;
import com.accountrix.banxi.model.user.User;
import com.plaid.client.model.*;
import com.plaid.client.request.PlaidApi;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.spring.security.AuthenticationContext;
import kotlin.Pair;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.vaadin.flow.helper.AsyncManager;
import org.vaadin.flow.helper.Task;
import retrofit2.Response;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Service
public class PlaidService {

    private final transient PlaidDevelopmentClient developmentClient;
    private final transient PlaidSandboxClient sandboxClient;
    private final transient AuthenticationContext authContext;
    private final transient PlaidItemService plaidItemService;
    private Hashtable<String, Institution> cachedInstitutions = new Hashtable<>();
    //TODO: ADD QUALIFIER OR SOMETHING TO FIGURE OUT WHICH CLIENT IS SAND AND WHICH IS DEV...
    public PlaidService(PlaidSandboxClient sandboxClient, PlaidDevelopmentClient developmentClient, AuthenticationContext authContext, PlaidItemService plaidItemService) {
        this.sandboxClient = sandboxClient;
        this.developmentClient = developmentClient;
        this.authContext = authContext;
        this.plaidItemService = plaidItemService;
    }

    private Optional<User> getUser() { return authContext.getAuthenticatedUser(User.class); }

    private String getPlaidClientID(String accessToken) { return (accessToken.contains("access-sandbox")) ? PlaidConfiguration.getPlaidSandboxClientID() : PlaidConfiguration.getPlaidDevelopmentClientID(); }
    private String getPlaidSecret(String accessToken) { return (accessToken.contains("access-sandbox")) ? PlaidConfiguration.getPlaidSandboxSecret() : PlaidConfiguration.getPlaidDevelopmentSecret(); }
    private PlaidApi client(String accessToken) { return (accessToken.contains("access-sandbox")) ? this.sandboxClient : this.developmentClient; }

    public List<Transaction> getTransactions(User user, LocalDate start, LocalDate end) {
        return getTransactions(user, null, start, end);
    }

    public List<Transaction> getTransactions(User user, String accountID, LocalDate start, LocalDate end) {
        if (accountID != null) {
            System.out.printf("[Transaction Search] For Account ID %s Between %s & %s\n", accountID, start.toString(), end.toString());
        } else {
            System.out.printf("[Transaction Search] For All Accounts Between %s & %s\n", start.toString(), end.toString());
        }
        List<Transaction> transactions = new ArrayList<>();
        user.getPlaidItems().forEach(plaidItem -> transactions.addAll(getTransactions(plaidItem.getAccessToken(), accountID, start, end)));
        return transactions;
    }

    private List<Transaction> getTransactions(String accessToken, String accountID, LocalDate start, LocalDate end) {

        List<Transaction> transactions = new ArrayList <Transaction>();

        TransactionsGetRequest request = new TransactionsGetRequest()
                .clientId(getPlaidClientID(accessToken))
                .secret(getPlaidSecret(accessToken))
                .accessToken(accessToken)
                .startDate(start)
                .endDate(end);
        if (accountID != null) { request.options(new TransactionsGetRequestOptions().accountIds(List.of(accountID))); }

        Response<TransactionsGetResponse> response = null;
        try { response = client(accessToken).transactionsGet(request).execute(); }
        catch (IOException e) {
            System.out.println(e);
            return transactions;
        }
        if (response.isSuccessful() && response.body() != null) {
            transactions.addAll(response.body().getTransactions());
            System.out.printf("[Transaction Search] %d Transactions Loaded Initially\n", response.body().getTransactions().size());
        } else {
            getItemDetails(accessToken).ifPresent(item -> {
                getInstitution(item.getInstitutionId(), accessToken).ifPresent(institution -> {
                    System.out.printf("[Transaction Search] Error Loading Initial Transactions Batch For %s\n", institution.getName());
                });
            });
            return transactions;
        }

        // Manipulate the offset parameter to paginate
        // transactions and retrieve all available data
        while (transactions.size() < response.body().getTotalTransactions()) {
            TransactionsGetRequestOptions options = new TransactionsGetRequestOptions()
                    .offset(transactions.size());
            try { response = client(accessToken).transactionsGet(request.options(options)).execute(); }
            catch (IOException e) { return transactions; }

            if (response.isSuccessful() && response.body() != null) {
                transactions.addAll(response.body().getTransactions());
                System.out.printf("[Transaction Search] %d Additional Transactions Loaded\n", response.body().getTransactions().size());
            } else {
                getItemDetails(accessToken).ifPresent(item -> {
                    getInstitution(item.getInstitutionId(), accessToken).ifPresent(institution -> {
                        System.out.printf("[Transaction Search] Error Loading Additional Transactions Batch For %s\n", institution.getName());
                    });
                });
                return transactions;
            }

        }

        return transactions;
    }

    public List<AccountBase> getAccounts(User user) {
        List<AccountBase> accounts = new ArrayList<>();
        user.getPlaidItems().forEach(plaidItem -> {
            //System.out.println(plaidItem);
            getItemDetails(plaidItem.getAccessToken()).ifPresent(item -> {
                //System.out.println(item);
                getInstitution(item.getInstitutionId(), plaidItem.getAccessToken()).ifPresent(institution -> {
                    //System.out.println(institution);
                    accounts.addAll(getInstitutionAccounts(plaidItem.getAccessToken()));
                });
            });
        });
        return accounts;
    }

    public Optional<Item> getItemDetails(String accessToken) {

        ItemGetRequest itemRequest = new ItemGetRequest()
                .accessToken(accessToken)
                .clientId(getPlaidClientID(accessToken))
                .secret(getPlaidSecret(accessToken));

        Response<ItemGetResponse> itemResponse = null;
        try { itemResponse = client(accessToken).itemGet(itemRequest).execute(); }
        catch (IOException e) { return Optional.empty(); }
        return (itemResponse.isSuccessful()) ? Optional.ofNullable(itemResponse.body().getItem()) : Optional.empty();
    }

    public Optional<Institution> getInstitution(String institutionID, String accessToken) {
        if (cachedInstitutions.containsKey(institutionID)) {
            System.out.printf("RETURNING CACHED INSTITUTION `%s`\n", institutionID);
            return Optional.ofNullable(cachedInstitutions.get(institutionID));
        }

        InstitutionsGetByIdRequest institutionRequest = new InstitutionsGetByIdRequest()
                .institutionId(institutionID)
                .addCountryCodesItem(CountryCode.US)
                .options(new InstitutionsGetByIdRequestOptions().includeOptionalMetadata(true))
                .clientId(getPlaidClientID(accessToken))
                .secret(getPlaidSecret(accessToken));

        Response<InstitutionsGetByIdResponse> institutionResponse = null;
        try { institutionResponse = client(accessToken).institutionsGetById(institutionRequest).execute(); }
        catch (IOException e) { return Optional.empty(); }
        if (institutionResponse.isSuccessful() && institutionResponse.body() != null) {
            cachedInstitutions.put(institutionID, institutionResponse.body().getInstitution());
        }
        return Optional.ofNullable(institutionResponse.body().getInstitution());
    }

    public List<AccountBase> getInstitutionAccounts(String accessToken) {

        AccountsBalanceGetRequest accountsRequest = new AccountsBalanceGetRequest()
                .accessToken(accessToken)
                .clientId(getPlaidClientID(accessToken))
                .secret(getPlaidSecret(accessToken));

        List<AccountBase> accounts = new ArrayList<>();
        Response<AccountsGetResponse> accountsResponse = null;
        try { accountsResponse = client(accessToken).accountsBalanceGet(accountsRequest).execute(); }
        catch (IOException e) { return accounts; }
        if (accountsResponse.isSuccessful() && accountsResponse.body() != null) {
            Item item = accountsResponse.body().getItem();
            accountsResponse.body().getAccounts().forEach(account -> {
                Optional<Institution> institution = getInstitution(item.getInstitutionId(), accessToken);
                account.setPersistentAccountId((institution.isEmpty()) ? "" : institution.get().getName());
                accounts.add(account);
            });
        }
        return accounts;
    }

    public Pair<List<TransactionStream>, List<TransactionStream>> getRecurringTransactions(User user) {
        List<TransactionStream> inflow = new ArrayList<>();
        List<TransactionStream> outflow = new ArrayList<>();

        for (PlaidItem plaidItem: user.getPlaidItems()) {
            List<String> accountIDs = new ArrayList<>();
            getInstitutionAccounts(plaidItem.getAccessToken()).forEach(account -> accountIDs.add(account.getAccountId()));
            var recurring = getRecurringTransactions(plaidItem.getAccessToken(), accountIDs);
            inflow.addAll(recurring.getFirst());
            outflow.addAll(recurring.getSecond());
        }

        return new Pair<>(inflow, outflow);
    }

    private Pair<List<TransactionStream>, List<TransactionStream>> getRecurringTransactions(String accessToken, List<String>accountIDs) {
        List<TransactionStream> inflow = new ArrayList<>();
        List<TransactionStream> outflow = new ArrayList<>();
        if (accountIDs.size() == 0) { return new Pair<>(inflow, outflow); }

        TransactionsRecurringGetRequest request = new TransactionsRecurringGetRequest()
                .clientId(getPlaidClientID(accessToken))
                .secret(getPlaidSecret(accessToken))
                .accessToken(accessToken)
                .accountIds(accountIDs);

        Response<TransactionsRecurringGetResponse> response = null;
        try { response = client(accessToken).transactionsRecurringGet(request).execute(); }
        catch (IOException e) { return new Pair<>(inflow, outflow); }
        if (response.isSuccessful() && response.body() != null) {
            inflow = response.body().getInflowStreams();
            outflow = response.body().getOutflowStreams();
        }
        return new Pair<>(inflow, outflow);
    }

    public Optional<String> createLinkToken(User user, boolean sandbox) {
        String fakeToken = String.format("access-%s", (sandbox) ? "sandbox" : "development");

        System.out.printf("createLinkToken for %s (%s)\n", user.getFullName(), user.getClientID());
        LinkTokenCreateRequestUser userRequest = new LinkTokenCreateRequestUser()
                .clientUserId(user.getClientID());

        LinkTokenCreateRequest linkTokenRequest = new LinkTokenCreateRequest()
                .clientId(getPlaidClientID(fakeToken))
                .secret(getPlaidSecret(fakeToken))
                .clientName("Banxi")
                .language("en")
                .countryCodes(Arrays.asList(CountryCode.US))
                .products(Arrays.asList(Products.TRANSACTIONS))
                .redirectUri("https://temecula.salmanburhan.com:9999/link/oauth")
                .user(userRequest);

        Response<LinkTokenCreateResponse> response = null;
        try { response = client(fakeToken).linkTokenCreate(linkTokenRequest).execute(); }
        catch (IOException e) { System.out.println("unable to create link token"); return Optional.empty(); }
        //System.out.println(response.toString());
        if (response.isSuccessful() && response.body() != null) {
            System.out.printf("link token: %s\n", response.body().getLinkToken());
            return Optional.ofNullable(response.body().getLinkToken());
        } else {
            return Optional.empty();
        }
    }

    public boolean exchangeToken(String publicToken, User user, boolean sandbox) {
        String fakeToken = String.format("access-%s", (sandbox) ? "sandbox" : "development");

        ItemPublicTokenExchangeRequest request = new ItemPublicTokenExchangeRequest()
                .clientId(getPlaidClientID(fakeToken))
                .secret(getPlaidSecret(fakeToken))
                .publicToken(publicToken);
        Response<ItemPublicTokenExchangeResponse> response = null;
        try { response = client(fakeToken).itemPublicTokenExchange(request).execute(); }
        catch (IOException e) { return false; }
        if (response.isSuccessful() && response.body() != null) {
            String itemID = response.body().getItemId();
            String accessToken = response.body().getAccessToken();
            PlaidItem newPlaidItem = new PlaidItem(itemID, accessToken, user);
            return plaidItemService.create(newPlaidItem) != null;
        } else {
            return false;
        }
    }

}
