package com.spsk1313.expensebudgeting.account.dto;

import com.spsk1313.expensebudgeting.account.AccountType;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponse(
        Long id,
        Long userId,
        String name,
        AccountType type,
        BigDecimal openingBalance,
        Instant createdAt,
        Instant updatedAt
){}
