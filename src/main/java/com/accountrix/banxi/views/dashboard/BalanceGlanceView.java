package com.accountrix.banxi.views.dashboard;

import com.accountrix.banxi.model.plaid.Account;
import com.accountrix.banxi.views.reusable.ActivityIndicator;
import com.accountrix.banxi.views.reusable.BalanceIndicator;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.Hashtable;

public class BalanceGlanceView extends VerticalLayout {

    private final ActivityIndicator activityIndicator = new ActivityIndicator(this);
    private final HorizontalLayout horizontalLayout = new HorizontalLayout();
    private Hashtable<String, Account> accounts = new Hashtable<>();

    public BalanceGlanceView() {

        Span title = new Span("At A Glance");
        title.addClassName(LumoUtility.FontSize.LARGE);

        this.add(title);
        this.add(horizontalLayout);
        this.setMinHeight(115, Unit.PIXELS);
    }

    public void setData(Hashtable<String, Account> accountList) {
        this.activityIndicator.stopAnimating();
        this.accounts = accountList;
        this.updateUI();
    }

    private void updateUI() {
        this.accounts.values().forEach(account -> {
            if (this.horizontalLayout.getChildren().noneMatch(component -> component.getId().orElse("-1").equals(account.getAccountId()))) {
                this.horizontalLayout.add(new BalanceIndicator(account));
            }
        });
    }
}
