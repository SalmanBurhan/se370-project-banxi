package com.accountrix.banxi.views.reusable.TransactionGrid;

import org.springframework.util.StringUtils;

import java.util.EnumSet;

public enum TransactionGridColumn {
    DATE,
    ACCOUNT,
    AMOUNT,
    NAME,
    LOCATION,
    CATEGORY,
    METHOD;

    @Override
    public String toString() {
        return StringUtils.capitalize(super.toString());
    }

    public static EnumSet<TransactionGridColumn> allColumns() {
        return EnumSet.allOf(TransactionGridColumn.class);
    }
}
