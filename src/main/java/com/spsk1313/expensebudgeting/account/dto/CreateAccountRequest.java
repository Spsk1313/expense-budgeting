package com.spsk1313.expensebudgeting.account.dto;

import com.spsk1313.expensebudgeting.account.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank
        @Size(max = 100)
        String name,
        @NotNull
        AccountType type,
        @NotNull
        BigDecimal openingBalance
) {
}
