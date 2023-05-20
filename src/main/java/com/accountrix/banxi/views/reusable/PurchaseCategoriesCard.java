package com.accountrix.banxi.views.reusable;

import com.accountrix.banxi.views.dashboard.CategoryChart;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.Hashtable;

public class PurchaseCategoriesCard extends VerticalLayout  {

    private final CategoryChart categoryChart = new CategoryChart();

    public PurchaseCategoriesCard() {
        layoutUI();
    }

    private void layoutUI() {
        addClassNames(LumoUtility.BorderRadius.LARGE, LumoUtility.Border.ALL, LumoUtility.BorderColor.CONTRAST_10);
        addClassName(LumoUtility.Margin.Bottom.AUTO);
        createHeaderView();

        categoryChart.addClassNames(LumoUtility.Margin.AUTO);
        this.add(categoryChart);
    }

    public void addCategories(Hashtable<String, Integer> data) {
        this.categoryChart.addCategories(data);
    }

    private void createHeaderView() {
        HorizontalLayout headerRow = new HorizontalLayout();

        Span titleLabel = new Span("Categories");
        titleLabel.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.TextColor.HEADER);

        headerRow.addClassNames(LumoUtility.Padding.Bottom.XSMALL, LumoUtility.Border.BOTTOM, LumoUtility.BorderColor.CONTRAST_10);
        headerRow.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        headerRow.setWidthFull();
        headerRow.add(titleLabel);

        add(headerRow);
    }


}
