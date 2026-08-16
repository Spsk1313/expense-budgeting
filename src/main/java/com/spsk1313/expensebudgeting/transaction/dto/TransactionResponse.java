package com.spsk1313.expensebudgeting.transaction.dto;

import com.spsk1313.expensebudgeting.transaction.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        TransactionType type,
        BigDecimal amount,

        Long accountId,
        Long sourceAccountId,
        Long destinationAccountId,
        Long categoryId,

        String description,
        LocalDate transactionDate,

        Instant createdAt,
        Instant updatedAt
) {
}
