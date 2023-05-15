package com.accountrix.banxi.views.dashboard;

import com.accountrix.banxi.model.user.User;
import com.accountrix.banxi.service.plaid.PlaidService;
import com.accountrix.banxi.views.MainLayout;
import com.accountrix.banxi.views.resuable.DateRangePickerView;
import com.plaid.client.model.AccountBase;
import com.plaid.client.model.Transaction;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import org.vaadin.flow.helper.AsyncManager;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@PermitAll
@PageTitle("Dashboard")
@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "dashboard", layout = MainLayout.class)
public class Dashboard extends VerticalLayout {

    private final transient AuthenticationContext authenticationContext;
    private transient User user;
    private final transient PlaidService plaid;

    private final Hashtable<String, AccountBase> accounts = new Hashtable<>();
    private final Hashtable<String, List<Transaction>> transactions = new Hashtable<>();
    private LocalDate oldestFetchDate = LocalDate.now().minusMonths(1);

    private final BalanceChart balanceChart = new BalanceChart();

    private final PieChartView pieChartCategories = new PieChartView();
    private HashMap<String, HashMap<String,Integer>> categoriesCountPerAccount;
    public Dashboard(AuthenticationContext authContext, PlaidService plaid) {

        this.authenticationContext = authContext;
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
        Board board = new Board();
        board.setSizeFull();
        board.addRow(balanceChart);
        //TODO:Pie doesn't show up yet :(
        board.addRow(pieChartCategories);

        DateRangePickerView dateRangeView = new DateRangePickerView();
        dateRangeView.getUpdateButton().setText("Update Range");
        dateRangeView.getUpdateButton().addClickListener(buttonClickEvent -> {
           System.out.println("range updated");
        });

        board.addRow(dateRangeView);

//        board.addRow(new H1("some board text"));

        add(board);
    }

    private void fetchData() {
        UI ui = UI.getCurrent();
        AsyncManager.register(this, task -> {
            fetchRecurringTransactions();
            task.push(() -> {
                System.out.println("Done Fetching Recurring Transactions");
            });
        });

        AsyncManager.register(this, task -> {
            fetchAccounts();
            accounts.values().forEach(account -> {
                fetchTransactions(account.getAccountId(), oldestFetchDate);
                Hashtable<LocalDate, Double> balances = calculateHistoricalBalanceForAccount(account.getAccountId());
                task.push(() -> {
                    System.out.println("Updating UI");
                    balanceChart.addAccount(String.format("%s %s", account.getPersistentAccountId(), account.getName()), balances);
                });
                countCategories(account.getAccountId());
            });
            task.push(()-> {
                pieChartCategories.addAccount(categoriesCountPerAccount);
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
        transactions.get(id).forEach(transaction -> transactionSums.merge(transaction.getDate(), transaction.getAmount(), Double::sum));
        System.out.printf("Transaction Sums for Account: %s %s\n", accounts.get(id).getPersistentAccountId(), accounts.get(id).getName());
        System.out.println(transactionSums);

        Hashtable<LocalDate, Double> balances = new Hashtable<>();
        Double balance = accounts.get(id).getBalances().getAvailable();
        List<LocalDate> dates = oldestFetchDate.datesUntil(LocalDate.now()).collect(Collectors.toList());
        Collections.reverse(dates);
        balances.put(LocalDate.now(), balance);
        for (LocalDate date : dates) {
            balance -= transactionSums.getOrDefault(date, Double.valueOf(0));
            balances.put(date, balance);
        }
        System.out.printf("Historical Balance For Account: %s %s\n", accounts.get(id).getPersistentAccountId(), accounts.get(id).getName());
        System.out.println(balances);
        return balances;
    }

    private HashMap<String,HashMap <String, Integer>> countCategories(String id) {

        //HashMap<String, HashMap<String, Integer>> categoriesCounter = new HashMap<>();

        List<Transaction> theTransactions = transactions.get(id);

        for(Transaction transaction : theTransactions ) {
            if (transaction.getCategory().isEmpty()){
                categoriesCountPerAccount.put("UNDEFINED", new HashMap<String, Integer>(){
                    {put(id,+1);}
                });
            }
            else {
                String category = transaction.getCategory().get(0);
                if (categoriesCountPerAccount.containsKey(category)) {
                    categoriesCountPerAccount.put(category, new HashMap<String, Integer>(){
                        {put(id,+1);}
                    });
                } else {
                    categoriesCountPerAccount.put(category, new HashMap<String, Integer>(){
                        {put(id,+1);}
                    });
                }
            }
        }

        return categoriesCountPerAccount;
    }


    private void populateBalanceChart(HashMap<LocalDate, Double> historicalAccountBalance) {

    }

}
