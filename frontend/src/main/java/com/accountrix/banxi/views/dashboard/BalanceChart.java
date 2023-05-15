package com.accountrix.banxi.views.dashboard;

import com.accountrix.banxi.views.reusable.ActivityIndicator;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.ChartVariant;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.html.Div;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class BalanceChart extends Div {

    private final ActivityIndicator activityIndicator;
    private final Chart chart = new Chart();

    public BalanceChart() {
        chart.getConfiguration().getChart().setType(ChartType.AREA);
        chart.getConfiguration().setTitle("Historical Balances");
        chart.getConfiguration().getyAxis().setTitle("Balance");
        chart.getConfiguration().getyAxis().setType(AxisType.LOGARITHMIC);
        chart.getConfiguration().getxAxis().getLabels().setFormat("{value:%b %d}");
        chart.addThemeVariants(ChartVariant.MATERIAL_GRADIENT);

        activityIndicator = new ActivityIndicator(chart);

        Tooltip tooltip = new Tooltip();
        tooltip.setValueDecimals(2);
        tooltip.setValuePrefix("$");
        tooltip.setShared(true);
        chart.getConfiguration().setTooltip(tooltip);

        add(chart);
    }

    public void addAccount(String accountName, Hashtable<LocalDate, Double> balanceHistory, ZoneId zoneID) {
        this.activityIndicator.stopAnimating();

        DataSeries series = new DataSeries();


        // TODO: FIX TOOLTIP NOT RENDERING.
//        PlotOptionsSeries plotOptionsSeries = new PlotOptionsSeries();
//        plotOptionsSeries.setTooltip(seriesTooltip);
//        series.setPlotOptions(plotOptionsSeries);

        series.setName(accountName);
        (new TreeMap<>(balanceHistory)).forEach((date, balance) -> series.add(dataSeriesItem(date, balance, zoneID)));
        chart.getConfiguration().addSeries(series);
    }

    private DataSeriesItem dataSeriesItem(LocalDate date, Double balance, ZoneId zoneID) {
        DataSeriesItem item = new DataSeriesItem(date.atStartOfDay(zoneID).toInstant(), balance);
        System.out.printf("DataSeriesItem<%s, %f>\n", item.getName(), item.getY().doubleValue());
        return item;
    }
}