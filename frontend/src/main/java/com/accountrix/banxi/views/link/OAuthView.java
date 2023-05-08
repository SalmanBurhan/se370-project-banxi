package com.accountrix.banxi.views.link;

import com.accountrix.banxi.model.user.User;
import com.accountrix.banxi.service.plaid.PlaidService;
import com.accountrix.banxi.views.MainLayout;
import com.accountrix.banxi.views.error.ErrorView;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import org.vaadin.flow.helper.AsyncManager;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@PageTitle("Banxi | Institution Link")
@Route(value = "/link/oauth", layout = MainLayout.class)
@PermitAll
public class OAuthView extends VerticalLayout {

    private final transient PlaidService plaid;

    private User currentUser;

    public OAuthView(AuthenticationContext authContext, PlaidService plaid) {
        this.plaid = plaid;

        if (authContext.getAuthenticatedUser(User.class).isEmpty()) {
            System.out.println("COULD NOT EXTRACT AUTHENTICATED USER");
        } else {
            this.currentUser = authContext.getAuthenticatedUser(User.class).get();
        }

        setAlignItems(Alignment.CENTER);
        addDependencies();
        setupLink();
        injectLinkHandler();
    }

    private void addDependencies() {
        UI.getCurrent().getPage().addJavaScript("https://cdn.plaid.com/link/v2/stable/link-initialize.js");
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
        UI ui = UI.getCurrent();
        AsyncManager.register(this, task -> {
            System.out.println("Exchanging Public Token " + publicToken + " for Access Token");
            if (plaid.exchangeToken(publicToken, currentUser, true)) {
                System.out.println("LINK COMPLETED SUCCESSFULLY, REDIRECTING TO DASHBOARD");
                task.push(() -> ui.navigate("dashboard"));
            } else {
                task.push(() -> ui.add(new ErrorView("The Link Flow Failed")));
            }
        });
    }

}