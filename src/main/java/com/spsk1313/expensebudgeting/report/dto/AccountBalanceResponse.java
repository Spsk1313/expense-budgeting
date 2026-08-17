package com.spsk1313.expensebudgeting.report.dto;

import java.math.BigDecimal;

public record AccountBalanceResponse(
        Long accountId,
        String accountName,
        BigDecimal openingBalance,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal totalTransfersIn,
        BigDecimal totalTransfersOut,
        BigDecimal currentBalance
) {
}
