package com.accountrix.banxi.views.dashboard;

import com.accountrix.banxi.views.reusable.ActivityIndicator;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.ChartVariant;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.component.html.Div;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class BalanceChart extends Div {

    private final ActivityIndicator activityIndicator;
    private final Chart chart = new Chart();

    public BalanceChart() {
        chart.getConfiguration().getChart().setType(ChartType.AREASPLINE);
        chart.getConfiguration().getyAxis().setTitle("Balance");
        chart.getConfiguration().getyAxis().setType(AxisType.LOGARITHMIC);
        chart.getConfiguration().getxAxis().getLabels().setFormat("{value:%b %d}");
        chart.addThemeVariants(ChartVariant.MATERIAL_GRADIENT);
        chart.setWidthFull();

        activityIndicator = new ActivityIndicator(chart);

        Tooltip tooltip = new Tooltip();
        tooltip.setValueDecimals(2);
        tooltip.setValuePrefix("$");
        tooltip.setShared(true);
        chart.getConfiguration().setTooltip(tooltip);

        add(chart);
    }

    public void addAccount(String accountName, Hashtable<LocalDate, Double> balanceHistory, ZoneId zoneID, @Nullable String hexColor) {
        this.activityIndicator.stopAnimating();

        PlotOptionsSeries options = new PlotOptionsSeries();
        if (hexColor != null) { options.setColor(new SolidColor(hexColor)); }
        options.setOpacity(0.3);

        DataSeries series = new DataSeries();
        series.setPlotOptions(options);

        series.setName(accountName);
        (new TreeMap<>(balanceHistory)).forEach((date, balance) -> series.add(dataSeriesItem(date, balance, zoneID)));
        chart.getConfiguration().addSeries(series);
    }

    private DataSeriesItem dataSeriesItem(LocalDate date, Double balance, ZoneId zoneID) {
        DataSeriesItem item = new DataSeriesItem(date.atStartOfDay(zoneID).toInstant(), balance);
        return item;
    }
}