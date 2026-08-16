package com.spsk1313.expensebudgeting.budget.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;

public record BudgetResponse(
        Long id,
        Long userId,
        Long categoryId,
        String categoryName,
        YearMonth month,
        BigDecimal limitAmount,
        Instant createdAt,
        Instant updatedAt
) {}
