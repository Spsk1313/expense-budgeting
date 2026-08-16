package com.spsk1313.expensebudgeting.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateExpenseRequest(
        @NotNull
        Long accountId,

        @NotNull
        Long categoryId,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal amount,

        String description,

        @NotNull
        LocalDate transactionDate
) {
}
