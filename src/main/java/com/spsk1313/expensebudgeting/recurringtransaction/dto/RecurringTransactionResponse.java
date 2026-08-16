package com.spsk1313.expensebudgeting.recurringtransaction.dto;

import com.spsk1313.expensebudgeting.recurringtransaction.RecurrenceFrequency;
import com.spsk1313.expensebudgeting.transaction.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record RecurringTransactionResponse(
        Long id,
        Long userId,
        TransactionType type,
        BigDecimal amount,

        Long accountId,
        Long sourceAccountId,
        Long destinationAccountId,
        Long categoryId,

        String description,

        RecurrenceFrequency frequency,
        LocalDate startDate,
        LocalDate nextRunDate,
        boolean active,

        Instant createdAt,
        Instant updatedAt
) {}
