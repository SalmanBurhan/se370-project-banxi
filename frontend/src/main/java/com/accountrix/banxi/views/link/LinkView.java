package com.accountrix.banxi.views.link;

import com.accountrix.banxi.model.user.User;
import com.accountrix.banxi.service.plaid.PlaidService;
import com.accountrix.banxi.views.MainLayout;
import com.accountrix.banxi.views.error.ErrorView;
import com.plaid.client.model.Institution;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;
import org.vaadin.flow.helper.AsyncManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@PageTitle("Banxi | Institution Link")
@Route(value = "/link", layout = MainLayout.class)
@PermitAll
public class LinkView extends VerticalLayout implements BeforeEnterObserver {

    private final transient AuthenticationContext authContext;

    private final transient PlaidService plaid;

    private User currentUser;

    private final Dialog activityIndicator = new Dialog();

    private boolean oauthDetected = false;

    public LinkView(AuthenticationContext authContext, PlaidService plaid) {
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
        AsyncManager.register(this, task -> {
            setupLink();
            task.push(() -> activityIndicator.setVisible(false));
        });
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
        Optional<String> linkToken = plaid.createLinkToken(this.currentUser);
        if (linkToken.isEmpty()) {
            add(new ErrorView("An Error Occurred Preparing The Link Flow."));
        } else {
            UI.getCurrent().getPage().executeJs("window.localStorage.setItem('link_token', $0);", linkToken.get());
            UI.getCurrent().getPage().executeJs("""
                window.exchangeToken = function exchangeToken(publicToken, element) {
                    console.log("Exchanging Public Token: " + publicToken + " for Access Token");
                    element.$server.exchangeToken(publicToken);
                }
            """);
            injectLinkHandler();
        }
    }

    private void injectLinkHandler() {
        UI.getCurrent().getPage().executeJs(
        """
                    window.handler = Plaid.create({
                        token: window.localStorage.getItem("link_token"),
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
                """, getElement()
        );
    }

    @ClientCallable
    public void exchangeToken(String publicToken) {
        IFrame processingAnimation = new IFrame("https://embed.lottiefiles.com/animation/127001");
        processingAnimation.setWidth(50, Unit.PERCENTAGE);
        add(processingAnimation);

        System.out.println("Exchanging Public Token " + publicToken + " for Access Token");
        try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException ignored) { }
        remove(processingAnimation);

        IFrame successAnimation = new IFrame("https://embed.lottiefiles.com/animation/97240");
        successAnimation.setWidth(50, Unit.PERCENTAGE);
        add(successAnimation);
        try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException ignored) { }
        remove(successAnimation);

        UI.getCurrent().navigate("dashboard");
    }

    private void addLinkedInstitutionsGrid() {
        Grid<Institution> grid = new Grid<>(Institution.class, false);
        grid.addColumn(Institution::getName).setHeader("Institution");
        grid.addColumn(Institution::getUrl).setHeader("URL");

        ArrayList<Institution> institutions = new ArrayList<>();
        this.currentUser.getPlaidItems().forEach(plaidItem -> {
            plaid.getItemDetails(plaidItem.getAccessToken()).ifPresent(itemDetails -> {
                plaid.getInstitution(itemDetails.getInstitutionId()).ifPresent(institutions::add);
            });
        });

        grid.setItems(institutions);
        grid.setAllRowsVisible(true);
        grid.setWidthFull();
        add(grid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        Map<String, List<String>> parameters = beforeEnterEvent
                .getLocation()
                .getQueryParameters()
                .getParameters();
//        if(parameters.containsKey("publicToken")) {
//            String publicToken = parameters.get("publicToken").get(0);
//            System.out.printf("Public Token Detected: %s\n", publicToken);
//            if (plaid.exchangeToken(publicToken, currentUser)) {
//                // TODO: Change This To Dashboard Page Once Implemented.
//                UI.getCurrent().navigate("transactions");
//            }
//        }
    }
}
