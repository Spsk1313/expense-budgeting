package com.spsk1313.expensebudgeting.report.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record BudgetStatusResponse(
        Long budgetId,
        Long categoryId,
        String categoryName,
        YearMonth month,
        BigDecimal limitAmount,
        BigDecimal spentAmount,
        BigDecimal remainingAmount,
        BigDecimal percentageUsed,
        boolean overspent
) {
}
