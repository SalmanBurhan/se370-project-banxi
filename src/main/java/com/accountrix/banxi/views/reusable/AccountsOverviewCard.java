package com.accountrix.banxi.views.reusable;

import com.accountrix.banxi.model.plaid.Account;
import com.accountrix.banxi.views.dashboard.BalanceChart;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Hashtable;

public class AccountsOverviewCard extends VerticalLayout {

    private final BalanceChart balanceChart = new BalanceChart();

    public AccountsOverviewCard() {
        layoutUI();
    }

    private void layoutUI() {
        addClassNames(LumoUtility.BorderRadius.LARGE, LumoUtility.Border.ALL, LumoUtility.BorderColor.CONTRAST_10);
        createHeaderView();
        this.add(balanceChart);
    }

    public void addAccount(Account account, Hashtable<LocalDate, Double> balanceHistory, ZoneId zoneID) {
        this.balanceChart.addAccount(account.getFullName(), balanceHistory, zoneID, account.getInstitutionColor());
        this.addBalanceRow(account);
    }


    private void createHeaderView() {
        HorizontalLayout headerRow = new HorizontalLayout();

        Span titleLabel = new Span("Your Accounts");
        titleLabel.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.TextColor.HEADER);

        headerRow.addClassNames(LumoUtility.Padding.Bottom.XSMALL, LumoUtility.Border.BOTTOM, LumoUtility.BorderColor.CONTRAST_10);
        headerRow.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        headerRow.setWidthFull();
        headerRow.add(titleLabel);

        add(headerRow);
    }

    private void addBalanceRow(Account account) {
        HorizontalLayout row = new HorizontalLayout();
        VerticalLayout leftStack = new VerticalLayout();
        VerticalLayout rightStack = new VerticalLayout();

        var icon = account.getLogo();
        icon.addClassNames(LumoUtility.Height.MEDIUM, LumoUtility.Width.MEDIUM);

        var accountNumber = new Span(String.format("*****%s", account.getAccountBase().getMask()));
        accountNumber.addClassNames(LumoUtility.TextColor.BODY, LumoUtility.FontWeight.BOLD, LumoUtility.FontSize.LARGE);

        var name = new Span(account.getName());
        name.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.MEDIUM, LumoUtility.FontSize.SMALL);

        var institution = new Span(account.getInstitutionName());
        institution.addClassNames(LumoUtility.TextColor.TERTIARY, LumoUtility.FontWeight.MEDIUM, LumoUtility.FontSize.SMALL);

        var balance = new Span(String.format("$%,.2f", account.getBalances().getCurrent()));
        balance.addClassNames(LumoUtility.FontWeight.BLACK, LumoUtility.TextAlignment.RIGHT, LumoUtility.FontSize.LARGE);

        leftStack.setSpacing(false);
        leftStack.addClassNames(LumoUtility.Padding.Top.NONE, LumoUtility.Padding.Bottom.NONE,
                LumoUtility.Margin.Top.NONE, LumoUtility.Margin.Bottom.NONE);

        rightStack.setSpacing(false);
        rightStack.addClassNames(LumoUtility.Padding.Top.NONE, LumoUtility.Padding.Bottom.NONE,
                LumoUtility.Margin.Top.NONE, LumoUtility.Margin.Bottom.NONE);
        rightStack.setHorizontalComponentAlignment(Alignment.END, balance);

        row.setSpacing(false);
        row.setPadding(true);
        row.addClassNames(LumoUtility.Border.BOTTOM, LumoUtility.BorderColor.CONTRAST_10,
                LumoUtility.Padding.Top.NONE, LumoUtility.Padding.Bottom.XSMALL,
                LumoUtility.Padding.MEDIUM, LumoUtility.Padding.Right.MEDIUM);
        row.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        row.setWidthFull();

        leftStack.add(accountNumber, name, institution);
        rightStack.add(balance);
        row.add(icon, leftStack, rightStack);
        add(row);
    }
}
