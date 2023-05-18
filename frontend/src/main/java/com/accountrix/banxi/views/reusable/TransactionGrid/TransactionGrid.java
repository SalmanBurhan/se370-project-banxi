package com.accountrix.banxi.views.reusable.TransactionGrid;

import com.accountrix.banxi.model.plaid.Account;
import com.plaid.client.model.AccountBase;
import com.plaid.client.model.Institution;
import com.plaid.client.model.Transaction;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.ListDataView;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

public class TransactionGrid extends Grid<Transaction> {
    private final Hashtable<String, Account> accounts = new Hashtable<>();
    public TransactionGrid() {
        super(Transaction.class, false);
        this.layoutGrid(TransactionGridColumn.allColumns());
    }

    public TransactionGrid(EnumSet<TransactionGridColumn> columns) {
        super(Transaction.class, false);
        this.layoutGrid(columns);
    }

    public GridListDataView<Transaction> setData(List<Transaction> transactionList, Hashtable<String, Account> accountList) {
        transactionList.sort(Comparator.comparing(Transaction::getDate).reversed());
        this.accounts.putAll(accountList);
        return this.setItems(transactionList);
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
                    new ComponentRenderer<>(transaction -> {
                        Account account = accounts.get(transaction.getAccountId());
                        Component icon = account.getLogo();
                        icon.getStyle().set("width", "30px");
                        icon.getStyle().set("height", "30px");
                        Text text = new Text ((icon instanceof Image) ? account.getName() : account.getFullName());
                        HorizontalLayout layout = new HorizontalLayout();
                        layout.add(icon, text);
                        return layout;
                    })
            ).setHeader("Account").setFlexGrow(1).setWidth("250px");
            case NAME -> this.addColumn(
                    new TextRenderer<>(transaction -> {
                        if (transaction.getMerchantName() != null) { return transaction.getMerchantName(); }
                        else { return transaction.getName(); }
                    })
            ).setTooltipGenerator(transaction -> {
                if (transaction.getMerchantName() != null) { return transaction.getName(); }
                else { return null; }
            }).setHeader("Name").setFlexGrow(1).setWidth("250px");
            case AMOUNT -> this.addColumn(
                    new ComponentRenderer<>(transaction -> {
                        String symbol = Currency.getInstance(transaction.getIsoCurrencyCode()).getSymbol();
                        String formatted = String.format("%s%,.2f", symbol, Math.abs(transaction.getAmount()));
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
                        return "";
                    })
            ).setTooltipGenerator(transaction -> {
                if (transaction.getCategory() != null && transaction.getCategory().size() > 0) {
                    return StringUtils.join(transaction.getCategory(), ", ");
                }
                return null;
            }).setHeader("Category").setFlexGrow(1).setWidth("150px");
            case LOCATION -> this.addColumn(
                    new TextRenderer<>(transaction -> {
                        String city = Objects.requireNonNullElse(transaction.getLocation().getCity(), "");
                        String state = Objects.requireNonNullElse(transaction.getLocation().getRegion(), "");
                        if (state.length() > 0) { state = String.format(", %s", state); }
                        return String.format("%s%s", city, state);
                    })
            ).setHeader("Location");
            case METHOD -> this.addColumn(Transaction::getPaymentChannel).setHeader("Method");
        }
    }
}
