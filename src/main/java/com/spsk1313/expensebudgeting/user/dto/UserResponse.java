package com.spsk1313.expensebudgeting.user.dto;

import java.time.Instant;

public record UserResponse(
        Long id,
        String name,
        String email,
        Instant createdAt,
        Instant updatedAt
) {
}
