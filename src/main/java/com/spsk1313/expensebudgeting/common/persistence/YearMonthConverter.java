package com.spsk1313.expensebudgeting.common.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDate;
import java.time.YearMonth;

@Converter
public class YearMonthConverter implements AttributeConverter<YearMonth, LocalDate> {

    @Override
    public LocalDate convertToDatabaseColumn(YearMonth yearMonth) {
        if(yearMonth == null) return null;

        return yearMonth.atDay(1);
    }

    @Override
    public YearMonth convertToEntityAttribute(LocalDate date) {
        if(date == null) return null;

        return YearMonth.from(date);
    }
}
