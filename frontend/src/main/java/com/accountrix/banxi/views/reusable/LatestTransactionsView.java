package com.accountrix.banxi.views.reusable;

import com.accountrix.banxi.model.plaid.Account;
import com.plaid.client.model.Transaction;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class LatestTransactionsView extends VerticalLayout {

    private final ActivityIndicator activityIndicator = new ActivityIndicator(this);
    private List<Transaction> transactions;
    private Hashtable<String, Account> accounts;

    public LatestTransactionsView() {
        layoutUI();
    }

    private void layoutUI() {
        addClassNames(LumoUtility.BorderRadius.LARGE, LumoUtility.Border.ALL, LumoUtility.BorderColor.CONTRAST_10);
        setMinHeight(200, Unit.PIXELS);
        createHeaderView();
    }

    public void setData(List<Transaction> transactionList, Hashtable<String, Account> accountList) {
        System.out.printf("[Latest Transactions View :: SET DATA :: %d TRANSACTIONS :: %d ACCOUNTS]\n", transactionList.size(), accountList.size());
        activityIndicator.stopAnimating();
        transactions = transactionList;
        accounts = accountList;
        render();
    }

    public void render() {
        transactions.sort(Comparator.comparing(Transaction::getDate).reversed());
        Iterator<Transaction> iterator = transactions.iterator();
        LocalDate date = null;
        Transaction transaction = null;
        while (iterator.hasNext()) {
            transaction = iterator.next();
            if (date == null || transaction.getDate().isBefore(date)) {
                date = transaction.getDate();
                createHeaderRow(date);
            }
            createDataRow(transaction);
        }
    }

    private void createDataRows(LocalDate date, List<Transaction> transactionList) {
        createHeaderRow(date);
        transactionList.forEach(this::createDataRow);
    }

    private void createHeaderRow(LocalDate date) {
        Span titleLabel = new Span(date.format(DateTimeFormatter.ofPattern("MMM dd")));
        titleLabel.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.TextColor.TERTIARY);
        titleLabel.addClassNames(LumoUtility.Margin.Top.LARGE);
        titleLabel.setWidthFull();
        add(titleLabel);
    }

    private void createDataRow(Transaction transaction) {
        HorizontalLayout row = new HorizontalLayout();
        VerticalLayout leftStack = new VerticalLayout();
        VerticalLayout rightStack = new VerticalLayout();

        var icon = accounts.get(transaction.getAccountId()).getLogo();
        icon.addClassNames(LumoUtility.Height.XLARGE, LumoUtility.Width.XLARGE);

        var name = parseTransactionName(transaction);
        name.addClassNames(LumoUtility.TextColor.BODY, LumoUtility.FontWeight.BOLD, LumoUtility.FontSize.LARGE);

        var accountName = parseTransactionAccount(accounts.get(transaction.getAccountId()), icon);
        name.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.MEDIUM);

        var category = parseTransactionCategory(transaction);
        category.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.FontWeight.MEDIUM);

        var amount = parseTransactionAmount(transaction);
        amount.addClassNames(LumoUtility.FontWeight.BLACK, LumoUtility.TextAlignment.RIGHT, LumoUtility.FontSize.LARGE);

        var date = parseTransactionDate(transaction);
        date.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.MEDIUM, LumoUtility.TextAlignment.RIGHT);

        leftStack.setSpacing(false);
        leftStack.addClassNames(LumoUtility.Padding.Top.NONE, LumoUtility.Padding.Bottom.NONE,
                                LumoUtility.Margin.Top.NONE, LumoUtility.Margin.Bottom.NONE);
        leftStack.add(name, accountName, category);

        rightStack.setSpacing(false);
        rightStack.addClassNames(LumoUtility.Padding.Top.NONE, LumoUtility.Padding.Bottom.NONE,
                LumoUtility.Margin.Top.NONE, LumoUtility.Margin.Bottom.NONE);
        rightStack.setHorizontalComponentAlignment(Alignment.END, amount, date);
        rightStack.add(amount, date);

        row.setSpacing(false);
        row.addClassNames(LumoUtility.Border.BOTTOM, LumoUtility.BorderColor.CONTRAST_10,
                          LumoUtility.Padding.Top.NONE, LumoUtility.Padding.Bottom.XSMALL,
                          LumoUtility.Padding.MEDIUM, LumoUtility.Padding.Right.MEDIUM);
        row.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        row.setWidthFull();
        row.add(icon, leftStack, rightStack);

        add(row);
    }

    private void createHeaderView() {
        HorizontalLayout headerRow = new HorizontalLayout();

        Span titleLabel = new Span("Latest Transactions");
        titleLabel.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.TextColor.HEADER);

        Button allTransactionsButton = new Button("See All", new Icon(VaadinIcon.CHEVRON_RIGHT_SMALL));
        allTransactionsButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        allTransactionsButton.addClassNames(LumoUtility.TextColor.PRIMARY, LumoUtility.Margin.Left.AUTO);
        allTransactionsButton.setIconAfterText(true);
        allTransactionsButton.addClickListener(e -> UI.getCurrent().navigate("transactions"));

        headerRow.addClassNames(LumoUtility.Padding.Bottom.XSMALL, LumoUtility.Border.BOTTOM, LumoUtility.BorderColor.CONTRAST_10);
        headerRow.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        headerRow.setWidthFull();
        headerRow.add(titleLabel, allTransactionsButton);

        add(headerRow);
    }


    private Span parseTransactionName(Transaction transaction) {
        return new Span((transaction.getMerchantName() != null) ? transaction.getMerchantName().toUpperCase() : transaction.getName().toUpperCase());
    }

    private Span parseTransactionAccount(Account account, Component icon) {
        return new Span ((icon instanceof Image) ? account.getName() : account.getFullName());
    }

    private Span parseTransactionAmount(Transaction transaction) {
        String symbol = Currency.getInstance(transaction.getIsoCurrencyCode()).getSymbol();
        String formatted = String.format("%s%,.2f", symbol, Math.abs(transaction.getAmount()));
        Span text = new Span(formatted);
        text.addClassName((transaction.getAmount() < 0) ? LumoUtility.TextColor.SUCCESS : LumoUtility.TextColor.ERROR);
        return text;
    }

    private Span parseTransactionDate(Transaction transaction) {
        return new Span(transaction.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
    }

    private Span parseTransactionCategory(Transaction transaction) {
        Span text = new Span();
        text.setText((transaction.getCategory() != null && transaction.getCategory().size() > 0)
                ? StringUtils.join(transaction.getCategory(), ", ") : "Uncategorized");
        return text;
    }

}
