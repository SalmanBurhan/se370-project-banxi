package com.accountrix.banxi.views.dashboard;

import com.accountrix.banxi.model.user.User;
import com.accountrix.banxi.service.plaid.PlaidService;
import com.accountrix.banxi.views.MainLayout;
import com.accountrix.banxi.views.reusable.BalanceIndicator;
import com.plaid.client.model.AccountBase;
import com.plaid.client.model.Transaction;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.ExtendedClientDetails;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
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

    private final Hashtable<String, AccountBase> accounts = new Hashtable<>();
    private final Hashtable<String, List<Transaction>> transactions = new Hashtable<>();
    private final LocalDate oldestFetchDate = LocalDate.now().minusMonths(1);
    private ZoneId userZoneID;

    private final BalanceChart balanceChart = new BalanceChart();
    private final CategoryChart categoryChart = new CategoryChart();
    private final RecentTransactionsView recentTransactionsView = new RecentTransactionsView();

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

        BalanceIndicator indicator = new BalanceIndicator("Chase Checking", "$1,234.19");

        Board board = new Board();
        board.setSizeFull();
        board.addRow(balanceChart);

        Row row2 = new Row();
        row2.add(categoryChart);
        row2.add(recentTransactionsView, 2);
        board.add(row2);

        board.addRow(new H1("some board text"));

        add(board);
    }

    private void fetchData() {
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
                fetchTransactions(account.getAccountId(), oldestFetchDate);
                Hashtable<String, Integer> categories = categorizeTransactions(account.getAccountId());
                Hashtable<LocalDate, Double> balances = calculateHistoricalBalanceForAccount(account.getAccountId());
                task.push(() -> {
                    System.out.println("Updating UI");
                    balanceChart.addAccount(String.format("%s %s", account.getPersistentAccountId(), account.getName()), balances, userZoneID);
                    categoryChart.addCategories(categories);
                    recentTransactionsView.setData(transactions.values().stream().flatMap(List::stream).collect(Collectors.toList()), accounts);
                });
            });
        });
    }

    private void fetchAccounts() {
        System.out.printf("Loading Accounts For %s\n", user.getFullName());
        List<AccountBase> userAccounts = plaid.getAccounts(user);
        userAccounts.forEach(account -> accounts.put(account.getAccountId(), account));
        System.out.printf("Loaded %d Accounts For %s\n", accounts.size(), user.getFullName());
    }

    private void fetchTransactions(String accountID, LocalDate oldest) {
        System.out.printf("Loading Transactions for Account: %s %s\n", accounts.get(accountID).getPersistentAccountId(), accounts.get(accountID).getName());
        List<Transaction> accountTransactions = plaid.getTransactions(user, accountID, oldest, LocalDate.now());
        transactions.put(accountID, accountTransactions);
        System.out.printf("Loaded %d Transactions for Account: %s %s\n", transactions.get(accountID).size(), accounts.get(accountID).getPersistentAccountId(), accounts.get(accountID).getName());
    }

    private void fetchRecurringTransactions() {
        var recurring = plaid.getRecurringTransactions(user);
        System.out.println(recurring.getFirst());
        System.out.println("-------------");
        System.out.println(recurring.getSecond());
    }

    private Hashtable<LocalDate, Double> calculateHistoricalBalanceForAccount(String id) {
        System.out.printf("Calculating Transaction Sums for Account: %s %s\n", accounts.get(id).getPersistentAccountId(), accounts.get(id).getName());
        HashMap<LocalDate, Double> transactionSums = new HashMap<>();
        transactions.get(id).forEach(transaction -> transactionSums.merge((transaction.getAuthorizedDate() == null) ? transaction.getDate() : transaction.getAuthorizedDate(), transaction.getAmount(), Double::sum));
        System.out.printf("Transaction Sums for Account: %s %s\n", accounts.get(id).getPersistentAccountId(), accounts.get(id).getName());
        System.out.println(transactionSums);
        System.out.println(new TreeMap<>(transactionSums));

        Hashtable<LocalDate, Double> balances = new Hashtable<>();
        Double balance = accounts.get(id).getBalances().getCurrent();
        List<LocalDate> dates = oldestFetchDate.datesUntil(LocalDate.now()).collect(Collectors.toList());
        Collections.reverse(dates);
        balances.put(LocalDate.now(), balance);
        for (LocalDate date : dates) {
            System.out.printf("[%s] %f - %f = %f\n", date, balance, transactionSums.getOrDefault(date, Double.valueOf(0)), (balance - transactionSums.getOrDefault(date, Double.valueOf(0))));
            balance -= Math.abs(transactionSums.getOrDefault(date, Double.valueOf(0)));
            balances.put(date, balance);
        }
        System.out.printf("Historical Balance For Account: %s %s\n", accounts.get(id).getPersistentAccountId(), accounts.get(id).getName());
        System.out.println(new TreeMap<>(balances));
        return balances;
    }

    private Hashtable<String, Integer> categorizeTransactions(String accountID) {
        System.out.printf("[Categorization] Categorizing Transactions For Account ID %s\n", accountID);
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
