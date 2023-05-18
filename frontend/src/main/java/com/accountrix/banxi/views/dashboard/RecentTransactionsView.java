package com.accountrix.banxi.views.dashboard;

import com.accountrix.banxi.model.plaid.Account;
import com.accountrix.banxi.views.reusable.ActivityIndicator;
import com.accountrix.banxi.views.reusable.TransactionGrid.TransactionGrid;
import com.accountrix.banxi.views.reusable.TransactionGrid.TransactionGridColumn;
import com.plaid.client.model.AccountBase;
import com.plaid.client.model.Institution;
import com.plaid.client.model.Transaction;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class RecentTransactionsView extends VerticalLayout {

    private final ActivityIndicator activityIndicator = new ActivityIndicator(this);
    private final TransactionGrid transactionGrid = new TransactionGrid(EnumSet.of(
            TransactionGridColumn.DATE,
            TransactionGridColumn.ACCOUNT,
            TransactionGridColumn.NAME,
            TransactionGridColumn.AMOUNT,
            TransactionGridColumn.LOCATION,
            TransactionGridColumn.CATEGORY
    ));

    public RecentTransactionsView() {
        this.layoutUI();
        setSpacing(false);
    }

    public void setData(List<Transaction> transactionList, Hashtable<String, Account> accountList) {
        this.activityIndicator.stopAnimating();
        this.transactionGrid.setData(transactionList, accountList);
        this.transactionGrid.setAllRowsVisible(true);
        this.transactionGrid.setHeightFull();
    }

    private void layoutUI() {
        Span transactionGridLabel = new Span("Recent Transactions");
        transactionGridLabel.addClassName(LumoUtility.FontSize.LARGE);

        Span transactionGridSubtitle = new Span("Across All Accounts");
        transactionGridSubtitle.addClassName(LumoUtility.FontSize.SMALL);
        transactionGridSubtitle.getStyle().set("color", "hsla(214, 42%, 18%, 0.69)");

        add(transactionGridLabel, transactionGridSubtitle, transactionGrid);
    }
}
