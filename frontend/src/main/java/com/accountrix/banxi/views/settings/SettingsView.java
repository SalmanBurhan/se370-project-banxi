package com.accountrix.banxi.views.settings;

import com.accountrix.banxi.model.plaid.item.PlaidItem;
import com.accountrix.banxi.model.user.User;
import com.accountrix.banxi.service.plaid.PlaidConfiguration;
import com.accountrix.banxi.service.plaid.PlaidService;
import com.accountrix.banxi.views.MainLayout;
import com.plaid.client.ApiClient;
import com.plaid.client.model.*;
import com.plaid.client.request.PlaidApi;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@PermitAll
@PageTitle("Settings")
@Route(value = "settings", layout = MainLayout.class)
public class SettingsView extends VerticalLayout {

    private final PlaidService plaid;

    private transient User currentUser;

    @Autowired
    public SettingsView(AuthenticationContext authContext, PlaidService plaid) {
        this.plaid = plaid;

        if (authContext.getAuthenticatedUser(User.class).isEmpty()) {
            System.out.println("COULD NOT EXTRACT AUTHENTICATED USER");
            UI.getCurrent().navigate("login");
        } else {
            this.currentUser = authContext.getAuthenticatedUser(User.class).get();
        }

        UI.getCurrent().access(this::layoutUI);
    }

    private void layoutUI() {
        TabSheet tabSheet = new TabSheet();
        tabSheet.setWidthFull();
        tabSheet.add("Profile", new Div());
        tabSheet.add("Linked Institutions", linkedInstitutionsTab());
        add(tabSheet);
    }

    private Component linkedInstitutionsTab() {
        Button linkButton = new Button("Link New Institution", VaadinIcon.INSTITUTION.create());
        linkButton.addClickListener(e -> {
            UI.getCurrent().navigate("link");
        });

        Grid<Institution> grid = new Grid<>(Institution.class, false);
        grid.addColumn(Institution::getName).setHeader("Institution").setFooter(linkButton);
        grid.addColumn(Institution::getUrl).setHeader("URL");

        ArrayList<Institution> institutions = new ArrayList<>();
        this.currentUser.getPlaidItems().forEach(plaidItem -> {
            plaid.getItemDetails(plaidItem.getAccessToken()).ifPresent(itemDetails -> {
                plaid.getInstitution(itemDetails.getInstitutionId(), plaidItem.getAccessToken()).ifPresent(institutions::add);
            });
        });

        grid.setItems(institutions);
        return grid;
    }
}
