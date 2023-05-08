package com.accountrix.banxi.views.transactions;

import com.accountrix.banxi.model.user.User;
import com.accountrix.banxi.service.plaid.PlaidService;
import com.accountrix.banxi.views.MainLayout;
import com.plaid.client.model.AccountBase;
import com.plaid.client.model.Transaction;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.vaadin.flow.helper.AsyncManager;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;

@PermitAll
@PageTitle("Transactions")
@Route(value = "transactions", layout = MainLayout.class)
public class TransactionsView extends VerticalLayout {

    private final transient AuthenticationContext authContext;

    private final transient PlaidService plaid;

    private User currentUser;

    private Dialog activityIndicator;
    private DatePicker startDate;
    private DatePicker endDate;
    private TextField searchField;
    private Grid<Transaction> transactionGrid;
    private final Hashtable<String, AccountBase> accounts = new Hashtable<>();

    public TransactionsView(AuthenticationContext authContext, PlaidService plaid) {

        this.authContext = authContext;
        this.plaid = plaid;

        if (authContext.getAuthenticatedUser(User.class).isEmpty()) {
            System.out.println("COULD NOT EXTRACT AUTHENTICATED USER");
        } else {
            this.currentUser = authContext.getAuthenticatedUser(User.class).get();
        }

        setMargin(true);
        layoutUI();
    }

    private void layoutUI() {
        add(new H2("Transactions"));
        layoutActivityIndicator();
        layoutDatePickers();
        layoutSearchField();
        layoutTransactionsGrid();
    }

    private void layoutActivityIndicator() {
        activityIndicator = new Dialog();

        activityIndicator.setCloseOnEsc(false);
        activityIndicator.setDraggable(false);
        activityIndicator.setCloseOnOutsideClick(false);
        activityIndicator.setResizable(false);

        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);

        Div progressBarLabel = new Div();
        progressBarLabel.setText("Fetching Transactions...");

        Div progressBarSubLabel = new Div();
        progressBarSubLabel.getStyle().set("font-size",
                "var(--lumo-font-size-xs)");
        progressBarSubLabel.setText("This may take a few seconds");

        activityIndicator.add(progressBarLabel, progressBar, progressBarSubLabel);
        add(activityIndicator);
    }

    private void layoutDatePickers() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();

        startDate = new DatePicker("Start Date");
        endDate = new DatePicker("End Date");

        startDate.setValue(LocalDate.now().minusWeeks(1));
        endDate.setValue(LocalDate.now());

        startDate.addValueChangeListener(e -> endDate.setMin(e.getValue()));
        endDate.addValueChangeListener(e -> startDate.setMax(e.getValue()));

        Button update = new Button("Update");
        update.addClickListener(e -> loadTransactions(startDate.getValue(), endDate.getValue()));

        horizontalLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        horizontalLayout.add(startDate, endDate, update);
        add(horizontalLayout);
    }

    private void layoutSearchField() {
        searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Search");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> transactionGrid.getListDataView().refreshAll());
        add(searchField);
    }

    private void layoutTransactionsGrid() {
        transactionGrid = new Grid<>(Transaction.class, false);
        transactionGrid.addColumn(Transaction::getDate).setHeader("Date");
        transactionGrid.addColumn(
                new TextRenderer<>(transaction -> {
                    AccountBase account = accounts.get(transaction.getAccountId());
                    return (account.getPersistentAccountId() != null
                            && account.getPersistentAccountId().isEmpty())
                            ? account.getName() : String.format("%s %s", account.getPersistentAccountId(), account.getName());
                })
        ).setHeader("Account");
        transactionGrid.addColumn(
                new TextRenderer<>(transaction -> {
                    if (transaction.getMerchantName() != null) { return transaction.getMerchantName(); }
                    else { return transaction.getName(); }
                })
        ).setHeader("Name");
        transactionGrid.addColumn(
                new ComponentRenderer<>(transaction -> {
                    String symbol = Currency.getInstance(transaction.getIsoCurrencyCode()).getSymbol();
                    String formatted = String.format("%s%.2f", symbol, Math.abs(transaction.getAmount()));
                    Label text = new Label(formatted);
                    text.addClassName((transaction.getAmount() < 0) ? LumoUtility.TextColor.SUCCESS : LumoUtility.TextColor.ERROR);
                    return text;
                })
        ).setHeader("Amount");
        transactionGrid.addColumn(
                new TextRenderer<>(transaction -> {
                    if (transaction.getCategory() != null && transaction.getCategory().size() > 0) {
                        return transaction.getCategory().get(transaction.getCategory().size() - 1);
                    }
                    else {
                        return "Unknown";
                    }
                })
        ).setHeader("Category");
        transactionGrid.addColumn(Transaction::getPaymentChannel).setHeader("Method");

        transactionGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        transactionGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        transactionGrid.setVisible(false);
        add(transactionGrid);
        loadTransactions();
    }

    private void addTransactionFilter(GridListDataView<Transaction> dataView) {
        dataView.addFilter(transaction -> {
            String searchTerm = searchField.getValue().trim();

            if (searchTerm.isEmpty())
                return true;

            boolean matchesDate = transactionMatch(transaction.getDate().toString(), searchTerm);
            boolean matchesName = transactionMatch(transaction.getName(), searchTerm);
            boolean matchesMethod = transactionMatch(transaction.getPaymentChannel().getValue(), searchTerm);

            AccountBase account = accounts.get(transaction.getAccountId());
            boolean matchesAccount = transactionMatch(
                    (account.getPersistentAccountId() != null && account.getPersistentAccountId().isEmpty())
                    ? account.getName() : String.format("%s %s", account.getPersistentAccountId(), account.getName()),
                    searchTerm
            );

            boolean matchesAmount = transactionMatch(
                    String.format("%s%.2f",
                            Currency.getInstance(transaction.getIsoCurrencyCode()).getSymbol(),
                            Math.abs(transaction.getAmount())
                    ),searchTerm);

            boolean matchesMerchantName = false;
            if (transaction.getMerchantName() != null) {
                matchesMerchantName = transactionMatch(transaction.getMerchantName(), searchTerm);
            }

            boolean matchesCategory = false;
            if (transaction.getCategory() != null)
                if (transaction.getCategory().size() > 0)
                    matchesCategory = transactionMatch(transaction.getCategory().get(transaction.getCategory().size() - 1), searchTerm);

            return matchesDate || matchesAccount || matchesName || matchesMerchantName || matchesAmount || matchesMethod || matchesCategory;
        });
    }

    private boolean transactionMatch(String value, String searchTerm) {
        return searchTerm == null || searchTerm.isEmpty() || value.toLowerCase().contains(searchTerm.toLowerCase());
    }

    private static Component createFilterHeader(String labelText, Consumer<String> filterChangeConsumer) {
        Label label = new Label(labelText);
        label.getStyle().set("padding-top", "var(--lumo-space-m)")
                .set("font-size", "var(--lumo-font-size-xs)");
        TextField textField = new TextField();
        textField.setValueChangeMode(ValueChangeMode.EAGER);
        textField.setClearButtonVisible(true);
        textField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        textField.setWidthFull();
        textField.getStyle().set("max-width", "100%");
        textField.addValueChangeListener(
                e -> filterChangeConsumer.accept(e.getValue()));
        VerticalLayout layout = new VerticalLayout(label, textField);
        layout.getThemeList().clear();
        layout.getThemeList().add("spacing-xs");

        return layout;
    }

    private void loadTransactions() { loadTransactions(startDate.getValue(), endDate.getValue()); }

    private void loadTransactions(LocalDate start, LocalDate end) {
        activityIndicator.open();
        AsyncManager.register(this, task -> {
            List<AccountBase> userAccounts = plaid.getAccounts(currentUser);
            userAccounts.forEach(account -> accounts.put(account.getAccountId(), account));
            System.out.printf("Loaded %d Accounts For %s\n", accounts.size(), currentUser.getFullName());
            List<Transaction> transactions = plaid.getTransactions(currentUser, start, end);
            System.out.printf("Loaded %d Transactions for %s\n", transactions.size(), currentUser.getFullName());
            task.push(() -> {
                System.out.println("Updating UI");
                activityIndicator.close();
                GridListDataView<Transaction> dataView = transactionGrid.setItems(transactions);
                addTransactionFilter(dataView);
                transactionGrid.setVisible(true);
            });
        });
    }

    private void updateUi(UI ui, String result) {
        ui.access(() -> {
            Notification.show(result);
        });
    }
}