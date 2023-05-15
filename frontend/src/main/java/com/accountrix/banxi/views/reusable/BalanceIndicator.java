package com.accountrix.banxi.views.reusable;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class BalanceIndicator extends VerticalLayout {
    public BalanceIndicator(String accountName, String currentBalance) {
        Span account = new Span(accountName);
        account.addClassName(LumoUtility.FontSize.SMALL);
        account.getStyle().set("color", "hsla(214, 42%, 18%, 0.69)");

        Span balance = new Span(currentBalance);
        balance.addClassName(LumoUtility.FontSize.MEDIUM);
        balance.getStyle().set("color", "hsla(214, 40%, 16%, 0.94)");

        setSpacing(false);
        addClassName(LumoUtility.BorderRadius.LARGE);
        addClassName(LumoUtility.Background.CONTRAST_5);
        add(account, balance);
    }
}
