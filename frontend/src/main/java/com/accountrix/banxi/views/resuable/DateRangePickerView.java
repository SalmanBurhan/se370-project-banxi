package com.accountrix.banxi.views.resuable;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.time.LocalDate;

public class DateRangePickerView extends HorizontalLayout {
    private final DatePicker startDate;
    private final DatePicker endDate;
    private final Button update;

    public DateRangePickerView() {
        startDate = new DatePicker("Start Date");
        endDate = new DatePicker("End Date");

        startDate.setValue(LocalDate.now().minusMonths(1));
        endDate.setValue(LocalDate.now());

        startDate.addValueChangeListener(e -> endDate.setMin(e.getValue()));
        endDate.addValueChangeListener(e -> startDate.setMax(e.getValue()));

        update = new Button("Update");
        //update.addClickListener(e -> loadTransactions(startDate.getValue(), endDate.getValue()));

        setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        add(startDate, endDate, update);
    }

    public Button getUpdateButton() { return this.update; }

    //public getDateRange(){return (startDate.getValue(), endDate.getValue());}
}
