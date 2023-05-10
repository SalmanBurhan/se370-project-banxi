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

        setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        add(startDate, endDate, update);
    }

    public Button getUpdateButton() { return this.update; }

    public DatePicker getStartDate() {
        return startDate;
    }

    public DatePicker getEndDate() {
        return endDate;
    }

    public LocalDate getStartDateValue() {
        return startDate.getValue();
    }

    public LocalDate getEndDateValue() {
        return endDate.getValue();
    }
}
