package com.spsk1313.expensebudgeting.report.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthlySummaryResponse(
        YearMonth month,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netCashFlow
) {
}
