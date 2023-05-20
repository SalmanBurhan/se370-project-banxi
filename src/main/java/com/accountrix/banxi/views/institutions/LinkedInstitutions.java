package com.accountrix.banxi.views.institutions;

import com.accountrix.banxi.model.user.User;
import com.accountrix.banxi.service.plaid.PlaidConfiguration;
import com.accountrix.banxi.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;

@PermitAll
@PageTitle("Linked Institutions")
@Route(value = "linked-institutions", layout = MainLayout.class)
public class LinkedInstitutions extends VerticalLayout {

    private final transient AuthenticationContext authContext;

    private final transient PlaidConfiguration plaidClient;

    private User currentUser;

    public LinkedInstitutions(AuthenticationContext authContext, PlaidConfiguration plaidClient) {
        this.authContext = authContext;
        this.plaidClient = plaidClient;

        if (authContext.getAuthenticatedUser(User.class).isEmpty()) {
            System.out.println("COULD NOT EXTRACT AUTHENTICATED USER");
            return;
        } else {
            this.currentUser = authContext.getAuthenticatedUser(User.class).get();
            UI.getCurrent().access(() -> {
                System.out.println("Loading Plaid Data...");
                this.loadData();
            });
        }

    }

    private void loadData() {

    }
}