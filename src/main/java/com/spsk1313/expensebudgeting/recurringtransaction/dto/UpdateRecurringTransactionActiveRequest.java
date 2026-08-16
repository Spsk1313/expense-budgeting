package com.spsk1313.expensebudgeting.recurringtransaction.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateRecurringTransactionActiveRequest(
       @NotNull Boolean active
) {}
