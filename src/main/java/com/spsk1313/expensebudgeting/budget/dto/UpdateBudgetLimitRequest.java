package com.spsk1313.expensebudgeting.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateBudgetLimitRequest(
        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal limitAmount
) {}