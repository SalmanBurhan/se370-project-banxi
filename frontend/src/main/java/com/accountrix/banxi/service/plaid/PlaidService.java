package com.accountrix.banxi.service.plaid;

import com.accountrix.banxi.model.user.User;
import com.plaid.client.model.*;
import com.plaid.client.request.PlaidApi;
import com.vaadin.flow.spring.security.AuthenticationContext;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import retrofit2.Response;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Service
public class PlaidService {

    private final transient PlaidApi client;
    private final transient AuthenticationContext authContext;
    private Hashtable<String, Institution> cachedInstitutions = new Hashtable<>();
    public PlaidService(PlaidApi plaidClient, AuthenticationContext authContext) {
        this.client = plaidClient;
        this.authContext = authContext;
    }

    private Optional<User> getUser() { return authContext.getAuthenticatedUser(User.class); }

    private List<Transaction> getTransactions(String accessToken, LocalDate start, LocalDate end) {

        List<Transaction> transactions = new ArrayList <Transaction>();

        TransactionsGetRequest request = new TransactionsGetRequest()
                .clientId(PlaidConfiguration.getPlaidClientID())
                .secret(PlaidConfiguration.getPlaidSecret())
                .accessToken(accessToken)
                .startDate(start)
                .endDate(end);
        Response<TransactionsGetResponse> response = null;
        try { response = client.transactionsGet(request).execute(); }
        catch (IOException e) { return transactions; }

        transactions.addAll(response.body().getTransactions());

        // Manipulate the offset parameter to paginate
        // transactions and retrieve all available data
        while (transactions.size() < response.body().getTotalTransactions()) {
            TransactionsGetRequestOptions options = new TransactionsGetRequestOptions()
                    .offset(transactions.size());
            try { response = client.transactionsGet(request.options(options)).execute(); }
            catch (IOException e) { return transactions; }
            transactions.addAll(response.body().getTransactions());
        }

        return transactions;
    }

    public List<Transaction> getTransactions(User user, LocalDate start, LocalDate end) {
        List<Transaction> transactions = new ArrayList<>();
        user.getPlaidItems().forEach(plaidItem -> transactions.addAll(getTransactions(plaidItem.getAccessToken(), start, end)));
        return transactions;
    }

    public List<AccountBase> getAccounts(User user) {
        List<AccountBase> accounts = new ArrayList<>();
        user.getPlaidItems().forEach(plaidItem -> {
            System.out.println(plaidItem);
            getItemDetails(plaidItem.getAccessToken()).ifPresent(item -> {
                System.out.println(item);
                getInstitution(item.getInstitutionId()).ifPresent(institution -> {
                    System.out.println(institution);
                    accounts.addAll(getInstitutionAccounts(plaidItem.getAccessToken()));
                });
            });
        });
        return accounts;
    }

    public Optional<Item> getItemDetails(String accessToken) {

        ItemGetRequest itemRequest = new ItemGetRequest()
                .accessToken(accessToken)
                .clientId(PlaidConfiguration.getPlaidClientID())
                .secret(PlaidConfiguration.getPlaidSecret());

        Response<ItemGetResponse> itemResponse = null;
        try { itemResponse = client.itemGet(itemRequest).execute(); }
        catch (IOException e) { return Optional.empty(); }
        return (itemResponse.isSuccessful()) ? Optional.ofNullable(itemResponse.body().getItem()) : Optional.empty();
    }

    public Optional<Institution> getInstitution(String institutionID) {
        if (cachedInstitutions.containsKey(institutionID)) {
            System.out.printf("RETURNING CACHED INSTITUTION `%s`\n", institutionID);
            return Optional.ofNullable(cachedInstitutions.get(institutionID));
        }

        InstitutionsGetByIdRequest institutionRequest = new InstitutionsGetByIdRequest()
                .institutionId(institutionID)
                .addCountryCodesItem(CountryCode.US)
                .options(new InstitutionsGetByIdRequestOptions().includeOptionalMetadata(true))
                .clientId(PlaidConfiguration.getPlaidClientID())
                .secret(PlaidConfiguration.getPlaidSecret());

        Response<InstitutionsGetByIdResponse> institutionResponse = null;
        try { institutionResponse = client.institutionsGetById(institutionRequest).execute(); }
        catch (IOException e) { return Optional.empty(); }
        if (institutionResponse.isSuccessful() && institutionResponse.body() != null) {
            cachedInstitutions.put(institutionID, institutionResponse.body().getInstitution());
        }
        return Optional.ofNullable(institutionResponse.body().getInstitution());
    }

    public List<AccountBase> getInstitutionAccounts(String accessToken) {

        AccountsGetRequest accountsRequest = new AccountsGetRequest()
                .accessToken(accessToken)
                .clientId(PlaidConfiguration.getPlaidClientID())
                .secret(PlaidConfiguration.getPlaidSecret());

        List<AccountBase> accounts = new ArrayList<>();
        Response<AccountsGetResponse> accountsResponse = null;
        try { accountsResponse = client.accountsGet(accountsRequest).execute(); }
        catch (IOException e) { return accounts; }
        if (accountsResponse.isSuccessful()) {
            Item item = accountsResponse.body().getItem();
            accountsResponse.body().getAccounts().forEach(account -> {
                Optional<Institution> institution = getInstitution(item.getInstitutionId());
                account.setPersistentAccountId((institution.isEmpty()) ? "" : institution.get().getName());
                accounts.add(account);
            });
        }
        return accounts;
    }

    /*
    public String createLinkToken() throws IOException {
        LinkTokenCreateRequestUser userRequest = new LinkTokenCreateRequestUser()
                .clientUserId("foo");
        LinkTokenCreateRequest linkTokenRequest = new LinkTokenCreateRequest()
                .clientName("Banxi")
                .language("en")
                .addProductsItem(Products.AUTH)
                .addProductsItem(Products.TRANSACTIONS)
                .addProductsItem(Products.BALANCE)
                .addCountryCodesItem(CountryCode.US)
                .user(userRequest)
                .clientId(PLAID_CLIENT_ID)
                .secret(PLAID_SECRET);
        Response<LinkTokenCreateResponse> response = plaidClient
                .linkTokenCreate(linkTokenRequest)
                .execute();
        return response.body().getLinkToken();
    }
    */

}
