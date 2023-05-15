package com.accountrix.banxi.views.reusable.TransactionGrid;

import com.plaid.client.model.AccountBase;
import com.plaid.client.model.Transaction;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.*;

public class TransactionGrid extends Grid<Transaction> {
    private final Hashtable<String, AccountBase> accounts = new Hashtable<>();

    public TransactionGrid() {
        super(Transaction.class, false);
        this.layoutGrid(TransactionGridColumn.allColumns());
    }

    public TransactionGrid(EnumSet<TransactionGridColumn> columns) {
        super(Transaction.class, false);
        this.layoutGrid(columns);
    }

    public void setData(List<Transaction> transactionList, Hashtable<String, AccountBase> accountList) {
        transactionList.sort(Comparator.comparing(Transaction::getDate).reversed());
        this.accounts.putAll(accountList);
        this.setItems(transactionList);
    }

    private void layoutGrid(EnumSet<TransactionGridColumn> columns) {
        columns.forEach(this::createColumn);
        this.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        this.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

    private void createColumn(TransactionGridColumn column) {
        switch (column) {
            case DATE -> this.addColumn(Transaction::getDate).setHeader("Date").setFlexGrow(0).setWidth("125px");
            case ACCOUNT -> this.addColumn(
                    new TextRenderer<>(transaction -> {
                        AccountBase account = accounts.get(transaction.getAccountId());
                        return (account.getPersistentAccountId() != null
                                && account.getPersistentAccountId().isEmpty())
                                ? account.getName() : String.format("%s %s", account.getPersistentAccountId(), account.getName());
                    })
            ).setHeader("Account").setFlexGrow(1).setWidth("250px");
            case NAME -> this.addColumn(
                    new TextRenderer<>(transaction -> {
                        if (transaction.getMerchantName() != null) { return transaction.getMerchantName(); }
                        else { return transaction.getName(); }
                    })
            ).setHeader("Name").setFlexGrow(1).setWidth("250px");
            case AMOUNT -> this.addColumn(
                    new ComponentRenderer<>(transaction -> {
                        String symbol = Currency.getInstance(transaction.getIsoCurrencyCode()).getSymbol();
                        String formatted = String.format("%s%.2f", symbol, Math.abs(transaction.getAmount()));
                        Label text = new Label(formatted);
                        text.addClassName((transaction.getAmount() < 0) ? LumoUtility.TextColor.SUCCESS : LumoUtility.TextColor.ERROR);
                        return text;
                    })
            ).setHeader("Amount");
            case CATEGORY -> this.addColumn(
                    new TextRenderer<>(transaction -> {
                        if (transaction.getCategory() != null && transaction.getCategory().size() > 0) {
                            return transaction.getCategory().get(transaction.getCategory().size() - 1);
                        }
                        else {
                            return "Unknown";
                        }
                    })
            ).setHeader("Category").setFlexGrow(1).setWidth("150px");
            case METHOD -> this.addColumn(com.plaid.client.model.Transaction::getPaymentChannel).setHeader("Method");
        }
    }
}
