package com.spsk1313.expensebudgeting.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DateRangeSummaryResponse(
        LocalDate from,
        LocalDate to,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netCashFlow
) {
}
