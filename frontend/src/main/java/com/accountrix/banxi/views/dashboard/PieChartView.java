package com.accountrix.banxi.views.dashboard;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.ChartVariant;
import com.vaadin.flow.component.charts.model.*;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class PieChartView extends Chart {

    public PieChartView() {
        getConfiguration().getChart().setType(ChartType.PIE);

        PlotOptionsPie options = new PlotOptionsPie();
        options.setInnerSize("0");
        options.setSize("75%");
        options.setCenter("50%", "50%");

        getConfiguration().setTitle("Purchase Categories");
        getConfiguration().setPlotOptions(options);

        addThemeVariants(ChartVariant.LUMO_GRADIENT);
    }

    public void addAccount(HashMap<String,HashMap<String, Integer>> categoriesCountPerAccount) {
        DataSeries series = new DataSeries();
        categoriesCountPerAccount.forEach((category, perAccount) ->{
            System.out.printf("building category %s\n",category);
            DataSeries drillDownSeries = new DataSeries();
            drillDownSeries.setId(category);
            Integer categoryTotal = 0;
            perAccount.forEach((account,count) -> {
                drillDownSeries.add(new DataSeriesItem(account,count));
            });
            for(Integer i = 0; i< drillDownSeries.size(); i++){
                categoryTotal = categoryTotal + drillDownSeries.get(i).getY().intValue();
            }
            DataSeriesItem mainItem = new DataSeriesItem(category,categoryTotal);

            series.addItemWithDrilldown(mainItem, drillDownSeries);

        });

        SeriesTooltip seriesTooltip = new SeriesTooltip();
        seriesTooltip.setPointFormat("function() { return this.x; }");

        // TODO: FIX TOOLTIP NOT RENDERING.
        PlotOptionsSeries plotOptionsSeries = new PlotOptionsSeries();
        plotOptionsSeries.setTooltip(seriesTooltip);
        series.setPlotOptions(plotOptionsSeries);

        //series.setName(accountName);
        //categoriesCountPerAccount.forEach((category, count) -> series.add(dataSeriesItem(category, count)));
        getConfiguration().addSeries(series);
    }
}