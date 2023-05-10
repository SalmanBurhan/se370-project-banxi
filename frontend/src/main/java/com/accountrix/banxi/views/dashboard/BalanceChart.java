package com.accountrix.banxi.views.dashboard;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.ChartVariant;
import com.vaadin.flow.component.charts.model.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Hashtable;

public class BalanceChart extends Chart {

    public BalanceChart() {

        getConfiguration().getChart().setType(ChartType.AREA);
        getConfiguration().setTitle("Historical Balances");
        getConfiguration().getyAxis().setTitle("Balance");
        getConfiguration().getxAxis().setTitle("Date");
        addThemeVariants(ChartVariant.LUMO_GRADIENT);
    }

    public void addAccount(String accountName, Hashtable<LocalDate, Double> balanceHistory) {
        DataSeries series = new DataSeries();

        SeriesTooltip seriesTooltip = new SeriesTooltip();
        seriesTooltip.setPointFormat("function() { return this.x; }");

        // TODO: FIX TOOLTIP NOT RENDERING.
        PlotOptionsSeries plotOptionsSeries = new PlotOptionsSeries();
        plotOptionsSeries.setTooltip(seriesTooltip);
        series.setPlotOptions(plotOptionsSeries);

        series.setName(accountName);
        balanceHistory.forEach((date, balance) -> series.add(dataSeriesItem(date, balance)));
        getConfiguration().addSeries(series);
    }

    private DataSeriesItem dataSeriesItem(LocalDate date, Double balance) {
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        return new DataSeriesItem(date.format(formatter), balance);
    }
}