package com.spsk1313.expensebudgeting.common.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class YearMonthConverterTest {

    private YearMonthConverter converter;

    @BeforeEach
    void setUp() {
        converter = new YearMonthConverter();
    }

   @ParameterizedTest
   @CsvSource({
           "2026, 8, 2026-08-01",
           "2024, 2, 2024-02-01",
           "2025, 12, 2025-12-01"
   })
    void convertToDatabaseColumnShouldConvertYearMonthToFirstDayOfMonth(int year, int month, String expectedDateStr) {
        YearMonth input = YearMonth.of(year, month);
        LocalDate expected = LocalDate.parse(expectedDateStr);

        LocalDate actual = converter.convertToDatabaseColumn(input);

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({
            "2026-08-01, 2026, 8",
            "2026-08-15, 2026, 8",
            "2024-02-29, 2024, 2"
    })
    void convertToEntityAttributeShouldConvertDateToYearMonth(String dbDateStr, int expectedYear, int expectedMonth) {
        LocalDate input = LocalDate.parse(dbDateStr);
        YearMonth expected = YearMonth.of(expectedYear, expectedMonth);

        YearMonth actual = converter.convertToEntityAttribute(input);

        assertEquals(expected, actual);

    }

    @Test
    void convertToDatabaseColumnWithNullShouldReturnNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttributeWithNullShouldReturnNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }
}
