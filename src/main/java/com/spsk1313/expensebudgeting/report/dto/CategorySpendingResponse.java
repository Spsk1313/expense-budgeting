package com.spsk1313.expensebudgeting.report.dto;

import java.math.BigDecimal;

public record CategorySpendingResponse(
        Long categoryId,
        String categoryName,
        BigDecimal totalAmount
) {
}
