package com.accountrix.banxi.views.dashboard;

import com.accountrix.banxi.model.plaid.Account;
import com.accountrix.banxi.model.user.User;
import com.accountrix.banxi.service.plaid.PlaidService;
import com.accountrix.banxi.views.MainLayout;
import com.accountrix.banxi.views.reusable.*;
import com.plaid.client.model.AccountBase;
import com.plaid.client.model.ClientProvidedEnrichedTransaction;
import com.plaid.client.model.Transaction;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.ExtendedClientDetails;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.vaadin.flow.helper.AsyncManager;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@PermitAll
@PageTitle("Dashboard")
@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "dashboard", layout = MainLayout.class)
public class Dashboard extends VerticalLayout {

    private transient User user;
    private final transient PlaidService plaid;

    private final Hashtable<String, Account> accounts = new Hashtable<>();
    private final Hashtable<String, List<Transaction>> transactions = new Hashtable<>();
    private final Hashtable<String, ClientProvidedEnrichedTransaction> enrichedTransactions = new Hashtable<>();
    private final LocalDate oldestFetchDate = LocalDate.now().minusMonths(1);
    private ZoneId userZoneID;

    private final BalanceChart balanceChart = new BalanceChart();
    private final CategoryChart categoryChart = new CategoryChart();
    private final RecentTransactionsView recentTransactionsView = new RecentTransactionsView();
    private final BalanceGlanceView balanceGlanceView = new BalanceGlanceView();
    private final HistoricalBalanceChart historicalBalanceChart = new HistoricalBalanceChart();
    private final LatestTransactionsView latestTransactionsView = new LatestTransactionsView();
    private final AccountsOverviewCard accountsOverviewCard = new AccountsOverviewCard();
    private final PurchaseCategoriesCard purchaseCategoriesCard = new PurchaseCategoriesCard();
    private final H1 totalBalanceLabel = new H1("$0.00");
    private Double totalBalance = 0.00;

    public Dashboard(AuthenticationContext authContext, PlaidService plaid) {

        this.plaid = plaid;

        if (authContext.getAuthenticatedUser(User.class).isEmpty()) {
            System.out.println("COULD NOT EXTRACT AUTHENTICATED USER");
        } else {
            this.user = authContext.getAuthenticatedUser(User.class).get();
        }

        layoutUI();
        fetchData();
    }

    private void layoutUI() {

        UI.getCurrent().getPage().retrieveExtendedClientDetails(this::setUserZoneId);

        Board board = new Board();
        board.setSizeFull();
        //board.addRow(historicalBalanceChart);
        //board.addRow(balanceChart, categoryChart);
        //board.addRow(balanceGlanceView);
        //board.addRow(latestTransactionsView);
        //board.addRow(recentTransactionsView);
        Row row = new Row();
        row.add(accountsOverviewCard);

        purchaseCategoriesCard.addClassNames(LumoUtility.Margin.Left.AUTO, LumoUtility.Margin.Right.AUTO);
        row.add(purchaseCategoriesCard);

        VerticalLayout totalBalanceContainer = new VerticalLayout();
        totalBalanceContainer.setSpacing(false);
        totalBalanceContainer.addClassName(LumoUtility.Padding.Bottom.LARGE);
        totalBalanceContainer.add(totalBalanceLabel);
        H4 totalBalanceSubtitle = new H4("Total Balance Across All Accounts");
        totalBalanceSubtitle.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.FontSize.SMALL);
        totalBalanceContainer.add(totalBalanceSubtitle);

        board.addRow(totalBalanceContainer);
        board.addRow(row);
        board.addRow(latestTransactionsView);
        add(board);
    }

    protected void fetchData() {
        UI ui = UI.getCurrent();
//        AsyncManager.register(this, task -> {
//            fetchRecurringTransactions();
//            task.push(() -> {
//                System.out.println("Done Fetching Recurring Transactions");
//            });
//        });

        AsyncManager.register(this, task -> {
            fetchAccounts();
            accounts.values().forEach(account -> {
                //fetchTransactions(account.getAccountId(), oldestFetchDate);
                fetchTransactions(account, oldestFetchDate);
                Hashtable<String, Integer> categories = categorizeTransactions(account.getAccountId());
                Hashtable<LocalDate, Double> balances = calculateHistoricalBalanceForAccount(account.getAccountId());
                task.push(() -> {
                    System.out.println("Updating UI");
                    balanceGlanceView.setData(accounts);
                    balanceChart.addAccount(account.getFullName(), balances, userZoneID, account.getInstitutionColor());
                    categoryChart.addCategories(categories);
                    recentTransactionsView.setData(transactions.values().stream().flatMap(List::stream).collect(Collectors.toList()), accounts);

                    totalBalanceLabel.setText(String.format("$%,.2f", totalBalance));
                    purchaseCategoriesCard.addCategories(categories);
                    accountsOverviewCard.addAccount(account, balances, userZoneID);
                });
            });
            task.push(() -> {
                latestTransactionsView.setData(transactions.values().stream().flatMap(List::stream).collect(Collectors.toList()), accounts);
            });
        });
    }

    protected void fetchAccounts() {
        System.out.printf("Loading Accounts For %s\n", user.getFullName());
        List<Account> userAccounts = plaid.getAccounts(user);
        System.out.printf("[List::Account] ==> %s\n", userAccounts);
        userAccounts.forEach(account -> accounts.put(account.getAccountId(), account));
        System.out.printf("Loaded %d Accounts For %s\n", accounts.size(), user.getFullName());
    }

    protected void fetchTransactions(Account account, LocalDate oldest) {
        System.out.printf("Loading Transactions for Account: %s\n", account.getFullName());
        List<Transaction> accountTransactions = plaid.getTransactions(account, oldest, LocalDate.now());
        transactions.put(account.getAccountId(), accountTransactions);
        System.out.printf("Loaded %d Transactions for Account: %s\n", accountTransactions.size(), account.getFullName());
    }

    private void fetchTransactions(String accountID, LocalDate oldest) {
        System.out.printf("Loading Transactions for Account: %s\n", accounts.get(accountID).getFullName());
        List<Transaction> accountTransactions = plaid.getTransactions(user, accountID, oldest, LocalDate.now());
        transactions.put(accountID, accountTransactions);
        plaid.enrichedTransactions(accountTransactions).forEach(enriched -> {
            System.out.printf("[ENRICHED TRANSACTION] ==> %s\n", enriched);
            enrichedTransactions.put(enriched.getId(), enriched);
        });
        System.out.printf("Loaded %d Transactions for Account: %s\n", transactions.get(accountID).size(), accounts.get(accountID).getFullName());
    }

    private void fetchRecurringTransactions() {
        var recurring = plaid.getRecurringTransactions(user);
        System.out.println(recurring.getFirst());
        System.out.println("-------------");
        System.out.println(recurring.getSecond());
    }

    private Hashtable<LocalDate, Double> calculateHistoricalBalanceForAccount(String id) {
        System.out.printf("Calculating Transaction Sums for Account: %s\n", accounts.get(id).getFullName());
        HashMap<LocalDate, Double> transactionSums = new HashMap<>();
        transactions.get(id).forEach(transaction -> transactionSums.merge((transaction.getAuthorizedDate() == null) ? transaction.getDate() : transaction.getAuthorizedDate(), transaction.getAmount(), Double::sum));
        System.out.printf("Transaction Sums for Account: %s\n", accounts.get(id).getFullName());

        System.out.println(new TreeMap<>(transactionSums));

        Hashtable<LocalDate, Double> balances = new Hashtable<>();
        Double balance = accounts.get(id).getBalances().getCurrent();
        totalBalance += balance;
        //List<LocalDate> dates = oldestFetchDate.datesUntil(LocalDate.now()).collect(Collectors.toList());
        //Collections.reverse(dates);

        balances.put(LocalDate.now(), balance);
        for (Map.Entry<LocalDate, Double> entry : transactionSums.entrySet()) {
            LocalDate date = entry.getKey();
            Double sum = entry.getValue();

            System.out.printf(
                    "[%s] %f - %f = %f\n",
                    date, balance, sum, (balance - sum)
            );
            balance -= sum;
            balances.put(date, balance);
        }
        balances.put(oldestFetchDate, balance);

//        for (LocalDate date : dates) {
//            System.out.printf("[%s] %f - %f = %f\n", date, balance, transactionSums.getOrDefault(date, (double) 0), (balance - transactionSums.getOrDefault(date, Double.valueOf(0))));
//            balance -= Math.abs(transactionSums.getOrDefault(date, (double) 0));
//            balances.put(date, balance);
//        }
        System.out.printf("Historical Balance For Account: %s\n", accounts.get(id).getFullName());
        System.out.println(new TreeMap<>(balances));
        return balances;
    }

    private Hashtable<String, Integer> categorizeTransactions(String accountID) {
        System.out.printf("[Categorization] Categorizing Transactions For Account: %s\n", accounts.get(accountID).getFullName());
        Hashtable<String, Integer> categories = new Hashtable<>();
        for (Transaction transaction : transactions.get(accountID)) {
            if (transaction.getCategory() != null && transaction.getCategory().size() > 0) {
                String category = transaction.getCategory().get(transaction.getCategory().size() - 1);
                categories.put(category, categories.get(category) == null ? 1 : categories.get(category) + 1);
            } else {
                categories.put("Uncategorized", categories.get("Uncategorized") == null ? 1 : categories.get("Uncategorized") + 1);
            }
        }
        System.out.println(categories);
        return categories;
    }

    private void setUserZoneId(ExtendedClientDetails details) { userZoneID = ZoneId.of(details.getTimeZoneId()); }

}
