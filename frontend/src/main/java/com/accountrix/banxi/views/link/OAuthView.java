package com.accountrix.banxi.views.link;

import com.accountrix.banxi.model.user.User;
import com.accountrix.banxi.service.plaid.PlaidService;
import com.accountrix.banxi.views.MainLayout;
import com.accountrix.banxi.views.error.ErrorView;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;

import java.util.Optional;

@PageTitle("Banxi | Institution Link")
@Route(value = "/link/oauth", layout = MainLayout.class)
@PermitAll
public class OAuthView extends VerticalLayout {

    private final transient AuthenticationContext authContext;

    private final transient PlaidService plaid;

    private User currentUser;

    private final Dialog activityIndicator = new Dialog();


    public OAuthView(AuthenticationContext authContext, PlaidService plaid) {
        this.authContext = authContext;
        this.plaid = plaid;

        if (authContext.getAuthenticatedUser(User.class).isEmpty()) {
            System.out.println("COULD NOT EXTRACT AUTHENTICATED USER");
        } else {
            this.currentUser = authContext.getAuthenticatedUser(User.class).get();
        }

        setAlignItems(Alignment.CENTER);
        layoutActivityIndicator();
        addDependencies();
        setupLink();
        injectLinkHandler();
        activityIndicator.setVisible(false);
        UI.getCurrent().getPage().executeJs("window.exchangeToken('someToken', $0);", getElement());
    }

    private void addDependencies() {
        UI.getCurrent().getPage().addJavaScript("https://cdn.plaid.com/link/v2/stable/link-initialize.js");
    }

    private void layoutActivityIndicator() {
        activityIndicator.setHeaderTitle("Preparing Link Flow");
        activityIndicator.add(new Text("This may take a few seconds"));
        activityIndicator.setVisible(true);
        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        activityIndicator.add(progressBar);
        add(activityIndicator);
    }

    private void setupLink() {
        UI.getCurrent().getPage().executeJs("""
                window.exchangeToken = function exchangeToken(publicToken, element) {
                    console.log("Exchanging Public Token: " + publicToken + " for Access Token");
                    element.$server.exchangeToken(publicToken);
                }
        """);
    }

    private void injectLinkHandler() {
        //window.location.replace("https://temecula.salmanburhan.com:9999/link?publicToken=" + publicToken);
        UI.getCurrent().getPage().executeJs(
                """
                    window.handler = Plaid.create({
                        token: window.localStorage.getItem("link_token"),
                        receivedRedirectUri: window.location.href,
                        onSuccess: async (publicToken, metadata) => {
                            window.exchangeToken(publicToken, $0);
                        },
                        onEvent: (eventName, metadata) => {
                            console.log("Event:", eventName);
                            console.log("Metadata:", metadata);
                        },
                        onExit: (error, metadata) => {
                            console.log(error, metadata);
                        },
                    });
                    window.handler.open();
                """
        );
    }

    @ClientCallable
    public void exchangeToken(String publicToken) {
        System.out.println("Exchanging Public Token " + publicToken + " for Access Token (OAUTH)");
    }

}