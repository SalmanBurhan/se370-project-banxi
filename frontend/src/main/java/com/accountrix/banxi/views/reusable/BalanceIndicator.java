package com.accountrix.banxi.views.reusable;

import com.accountrix.banxi.model.plaid.Account;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class BalanceIndicator extends HorizontalLayout {
    public BalanceIndicator(Account account) {

        this.setId(account.getAccountId());
        this.setPadding(true);
        this.setAlignItems(FlexComponent.Alignment.STRETCH);
        this.setSpacing(false);
        this.getThemeList().add("spacing-s");
        this.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        this.addClassName(LumoUtility.BorderRadius.LARGE);
        this.addClassName(LumoUtility.Background.CONTRAST_5);

        Component logo = account.getLogo();
        logo.getStyle().set("width", "75px");
        logo.getStyle().set("height", "75px");
        this.add(logo);

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(false);

        Span institution = new Span(account.getInstitutionName());
        institution.addClassName(LumoUtility.FontSize.LARGE);
        institution.addClassName(LumoUtility.FontWeight.BOLD);
        institution.getStyle().set("color", "hsla(214, 42%, 18%, 0.69)");

        Span name = new Span(account.getName());
        name.addClassName(LumoUtility.FontSize.SMALL);
        name.getStyle().set("color", "hsla(214, 42%, 18%, 0.69)");

        Span balance = new Span(String.format("$%,.2f", account.getBalances().getCurrent()));
        balance.addClassName(LumoUtility.FontSize.XLARGE);
        balance.addClassName(LumoUtility.FontWeight.BLACK);
        balance.getStyle().set("color", "hsla(214, 40%, 16%, 0.94)");

        verticalLayout.add(institution, name, balance);
        this.add(verticalLayout);
    }
}
