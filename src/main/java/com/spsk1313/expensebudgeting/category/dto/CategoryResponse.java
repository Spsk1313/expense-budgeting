package com.spsk1313.expensebudgeting.category.dto;

import com.spsk1313.expensebudgeting.category.CategoryType;

import java.time.Instant;

public record CategoryResponse(
        Long id,
        Long userId,
        String name,
        CategoryType type,
        Instant createdAt,
        Instant updatedAt
) {
}
