package com.accountrix.banxi.views;


import com.accountrix.banxi.components.appnav.AppNav;
import com.accountrix.banxi.components.appnav.AppNavItem;
import com.accountrix.banxi.service.security.SecurityService;
import com.accountrix.banxi.views.dashboard.Dashboard;
import com.accountrix.banxi.views.link.LinkView;
import com.accountrix.banxi.views.transactions.TransactionsView;
import com.accountrix.banxi.views.settings.SettingsView;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.time.LocalDate;
import java.util.Date;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private final SecurityService securityService;
    private H2 viewTitle;

    public MainLayout(SecurityService securityService) {
        this.securityService = securityService;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {

        Avatar avatar = new Avatar(securityService.getAuthenticatedUser().getFullName());

        H1 appName = new H1("Banxi");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        Header header = new Header(avatar, appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private AppNav createNavigation() {
        // AppNav is not yet an official component.
        // For documentation, visit https://github.com/vaadin/vcf-nav#readme

        AppNav nav = new AppNav();

        //nav.addItem(new AppNavItem("Link", LinkView.class));
        nav.addItem(new AppNavItem("Dashboard", Dashboard.class, LineAwesomeIcon.CHART_LINE_SOLID.create()));
        nav.addItem(new AppNavItem("Transactions", TransactionsView.class, LineAwesomeIcon.RECEIPT_SOLID.create()));
        nav.addItem(new AppNavItem("Settings", SettingsView.class, LineAwesomeIcon.TOOLS_SOLID.create()));

        return nav;
    }

    private Footer createFooter() {
        Footer footer = new Footer();
        VerticalLayout layout = new VerticalLayout();

        Button logout = new Button("Log Out", event -> securityService.logout());
        logout.setWidthFull();
        layout.add(logout);

        Span copyright = new Span();
        copyright.getElement().setProperty("innerHTML", String.format("© %d Banxi.<br/>All Rights Reserved.", LocalDate.now().getYear()));
        copyright.getElement().getStyle().set("font-size", "var(--lumo-font-size-xxs)");
        layout.add(copyright);

        footer.add(layout);
        return footer;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
