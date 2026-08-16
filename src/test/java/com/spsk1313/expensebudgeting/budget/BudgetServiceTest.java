package com.spsk1313.expensebudgeting.budget;

import com.spsk1313.expensebudgeting.budget.dto.BudgetResponse;
import com.spsk1313.expensebudgeting.budget.dto.CreateBudgetRequest;
import com.spsk1313.expensebudgeting.budget.exception.DuplicateBudgetException;
import com.spsk1313.expensebudgeting.category.Category;
import com.spsk1313.expensebudgeting.category.CategoryRepository;
import com.spsk1313.expensebudgeting.category.CategoryType;
import com.spsk1313.expensebudgeting.category.exception.CategoryNotFoundException;
import com.spsk1313.expensebudgeting.user.User;
import com.spsk1313.expensebudgeting.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private BudgetService budgetService;

    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("200.00");
    private static final YearMonth DEFAULT_MONTH = YearMonth.of(2026, 8);

    @Test
    void createBudgetWithValidResourcesShouldSaveBudget() {
        User user = createUser();
        Category category = createCategory(user);

        CreateBudgetRequest req = new CreateBudgetRequest(
                10L,
                DEFAULT_MONTH,
                DEFAULT_AMOUNT
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndUser_Id(10L, 1L)).thenReturn(Optional.of(category));
        when(budgetRepository.save(any(Budget.class))).thenAnswer((invocation -> invocation.getArgument(0)));

        BudgetResponse response = budgetService.createBudget(1L, req);

        assertEquals("Groceries", response.categoryName());
        assertEquals(DEFAULT_AMOUNT, response.limitAmount());
        assertEquals(DEFAULT_MONTH, response.month());

        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    void createBudgetWithCategoryOwnedByAnotherUserShouldFail() {
        User user1 = createUser();

        CreateBudgetRequest req = new CreateBudgetRequest(
                10L,
                DEFAULT_MONTH,
                DEFAULT_AMOUNT
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(categoryRepository.findByIdAndUser_Id(10L, 1L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> {
            budgetService.createBudget(1L, req);
        });

        verify(budgetRepository, never()).save(any());
    }

    @Test
    void createBudgetWhenBudgetAlreadyExistsShouldThrowDuplicateBudgetException() {
        User user = createUser();
        Category category = createCategory(user);

        CreateBudgetRequest req = new CreateBudgetRequest(
                10L,
                DEFAULT_MONTH,
                DEFAULT_AMOUNT
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndUser_Id(10L, 1L)).thenReturn(Optional.of(category));
        when(budgetRepository.existsByUser_IdAndCategory_IdAndMonth(1L, 10L, DEFAULT_MONTH)).thenReturn(true);

        assertThrows(DuplicateBudgetException.class, () -> budgetService.createBudget(1L, req));

        verify(budgetRepository, never()).save(any());
    }

    private User createUser() {
        return new User("Sahil", "sahil@example.com");
    }

    private Category createCategory(
            User user
    ) {
        return new Category(user, "Groceries", CategoryType.EXPENSE);
    }
}
