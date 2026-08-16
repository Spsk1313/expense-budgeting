package com.spsk1313.expensebudgeting.category.dto;

import com.spsk1313.expensebudgeting.category.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
        @NotBlank
        @Size(max = 100)
        String name,
        @NotNull
        CategoryType type
) {
}
