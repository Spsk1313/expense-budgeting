package com.spsk1313.expensebudgeting.budget;

import com.spsk1313.expensebudgeting.category.Category;
import com.spsk1313.expensebudgeting.category.CategoryType;
import com.spsk1313.expensebudgeting.user.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

public class BudgetTest {

    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("200.00");
    private static final YearMonth DEFAULT_MONTH = YearMonth.of(2026, 8);

    @Test
    void createBudgetWithValidDataShouldCreateBudget() {
        User user = createUser();
        Category category = createCategory(user, "Groceries", CategoryType.EXPENSE);

        Budget budget = new Budget(user, category, DEFAULT_MONTH, DEFAULT_AMOUNT);

        assertAll(
                () -> assertSame(user, budget.getUser()),
                () -> assertSame(category, budget.getCategory()),
                () -> assertEquals(DEFAULT_MONTH, budget.getMonth()),
                () -> assertEquals(DEFAULT_AMOUNT, budget.getLimitAmount())
        );
    }

    @Test
    void createBudgetWithIncomeCategoryShouldRejectCategory() {
        User user = createUser();
        Category category = createCategory(user, "Salary", CategoryType.INCOME);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new Budget(user, category, DEFAULT_MONTH, DEFAULT_AMOUNT);
        });

        assertEquals("Budget requires an expense category", ex.getMessage());
    }

    @Test
    void createBudgetWithCategoryOwnedByAnotherUserShouldRejectCategory() {
        User user1 = createUser();
        User user2 = new User("John", "john@example.com");
        Category category = createCategory(user2, "Groceries", CategoryType.EXPENSE);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new Budget(user1, category, DEFAULT_MONTH, DEFAULT_AMOUNT);
        });

        assertEquals("Category must belong to the budget user", ex.getMessage());
    }

    @Test
    void createBudgetWithZeroLimitShouldRejectLimit() {
        User user = createUser();
        Category category = createCategory(user, "Groceries", CategoryType.EXPENSE);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new Budget(user, category, DEFAULT_MONTH, BigDecimal.ZERO);
        });

        assertEquals("Budget limit must be greater than 0", ex.getMessage());
    }

    @Test
    void createBudgetWithNegativeLimitShouldRejectLimit() {
        User user = createUser();
        Category category = createCategory(user, "Groceries", CategoryType.EXPENSE);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new Budget(user, category, DEFAULT_MONTH, new BigDecimal("-10.00"));
        });

        assertEquals("Budget limit must be greater than 0", ex.getMessage());
    }

    @Test
    void changeLimitWithValidAmountShouldUpdateLimit() {
        User user = createUser();
        Category category = createCategory(
                user,
                "Groceries",
                CategoryType.EXPENSE
        );

        Budget budget = new Budget(
                user,
                category,
                DEFAULT_MONTH,
                DEFAULT_AMOUNT
        );

        BigDecimal newLimit = new BigDecimal("350.00");

        budget.changeLimit(newLimit);

        assertEquals(newLimit, budget.getLimitAmount());
    }

    private User createUser() {
        return new User("Sahil", "sahil@example.com");
    }

    private Category createCategory(
            User user,
            String name,
            CategoryType type
    ) {
        return new Category(user, name, type);
    }
}
