package com.accountrix.banxi.views.dashboard;

import com.accountrix.banxi.views.reusable.ActivityIndicator;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.ChartVariant;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.charts.model.style.Color;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.component.charts.model.style.Style;
import com.vaadin.flow.component.charts.themes.LumoDarkTheme;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.Hashtable;

public class CategoryChart extends Div {

    private final ActivityIndicator activityIndicator;
    private final Chart chart = new Chart();
    private final DataSeries series = new DataSeries();

    public CategoryChart() {
        chart.getConfiguration().getChart().setType(ChartType.PIE);
//        chart.getConfiguration().setTitle("Purchase Categories");
//        chart.getConfiguration().setSubTitle("Across All Accounts");
        chart.getConfiguration().getxAxis().setType(AxisType.CATEGORY);
        chart.getConfiguration().getChart().setBackgroundColor(new SolidColor(0,0,0,0));

        PlotOptionsPie options = new PlotOptionsPie();
        options.setInnerSize("50%");
        options.setBorderWidth(0);
        chart.getConfiguration().setPlotOptions(options);

        chart.addThemeVariants(ChartVariant.MATERIAL_GRADIENT);

        activityIndicator = new ActivityIndicator(chart);

        Tooltip tooltip = new Tooltip();
        tooltip.setPointFormat("{point.percentage:%02.2f}%");
        tooltip.setShared(true);
        chart.getConfiguration().setTooltip(tooltip);

        chart.getConfiguration().addSeries(series);

        add(chart);
    }

    public void addCategories(Hashtable<String, Integer> data) {
        activityIndicator.stopAnimating();
        data.forEach((category, count) -> {
            if (series.get(category) != null) {
                DataSeriesItem item = series.get(category);
                item.setY(item.getY().intValue() + count);
                series.update(item);
            } else {
                series.add(new DataSeriesItem(category, count));
            }
        });
    }

}
