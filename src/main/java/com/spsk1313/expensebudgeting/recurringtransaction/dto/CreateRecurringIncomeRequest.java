package com.spsk1313.expensebudgeting.recurringtransaction.dto;

import com.spsk1313.expensebudgeting.recurringtransaction.RecurrenceFrequency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateRecurringIncomeRequest(
        @NotNull Long accountId,
        @NotNull Long categoryId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        String description,
        @NotNull RecurrenceFrequency frequency,
        @NotNull LocalDate startDate
) {
}
