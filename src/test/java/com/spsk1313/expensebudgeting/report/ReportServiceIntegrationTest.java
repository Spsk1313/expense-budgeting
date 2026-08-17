package com.spsk1313.expensebudgeting.report;

import com.spsk1313.expensebudgeting.account.Account;
import com.spsk1313.expensebudgeting.account.AccountRepository;
import com.spsk1313.expensebudgeting.account.AccountType;
import com.spsk1313.expensebudgeting.budget.Budget;
import com.spsk1313.expensebudgeting.budget.BudgetRepository;
import com.spsk1313.expensebudgeting.category.Category;
import com.spsk1313.expensebudgeting.category.CategoryRepository;
import com.spsk1313.expensebudgeting.category.CategoryType;
import com.spsk1313.expensebudgeting.report.dto.BudgetStatusResponse;
import com.spsk1313.expensebudgeting.transaction.Transaction;
import com.spsk1313.expensebudgeting.transaction.TransactionRepository;
import com.spsk1313.expensebudgeting.user.User;
import com.spsk1313.expensebudgeting.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class ReportServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void cleanDatabase() {
        transactionRepository.deleteAll();
        budgetRepository.deleteAll();
        categoryRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void getBudgetStatusShouldCalculateStatusForAllBudgets() {

        User user = userRepository.save(
                new User("Sahil", "sahil@example.com")
        );

        Account account = accountRepository.save(
                new Account(
                        user,
                        "Chequing",
                        AccountType.CHEQUING,
                        new BigDecimal("1000.00")
                )
        );

        Category groceries = categoryRepository.save(
                new Category(
                        user,
                        "Groceries",
                        CategoryType.EXPENSE
                )
        );

        Category dining = categoryRepository.save(
                new Category(
                        user,
                        "Dining",
                        CategoryType.EXPENSE
                )
        );

        Category gym = categoryRepository.save(
                new Category(
                        user,
                        "Gym",
                        CategoryType.EXPENSE
                )
        );

        YearMonth month = YearMonth.of(2026, 8);

        budgetRepository.save(
                new Budget(
                        user,
                        groceries,
                        month,
                        new BigDecimal("500.00")
                )
        );

        budgetRepository.save(
                new Budget(
                        user,
                        dining,
                        month,
                        new BigDecimal("300.00")
                )
        );

        budgetRepository.save(
                new Budget(
                        user,
                        gym,
                        month,
                        new BigDecimal("100.00")
                )
        );

        // Groceries = $450
        transactionRepository.save(
                Transaction.createExpense(
                        account,
                        groceries,
                        new BigDecimal("200.00"),
                        "Groceries",
                        LocalDate.of(2026, 8, 5)
                )
        );

        transactionRepository.save(
                Transaction.createExpense(
                        account,
                        groceries,
                        new BigDecimal("250.00"),
                        "More groceries",
                        LocalDate.of(2026, 8, 15)
                )
        );

        // Dining = $400
        transactionRepository.save(
                Transaction.createExpense(
                        account,
                        dining,
                        new BigDecimal("400.00"),
                        "Restaurants",
                        LocalDate.of(2026, 8, 20)
                )
        );

        List<BudgetStatusResponse> statuses =
                reportService.getBudgetStatus(
                        user.getId(),
                        month
                );

        assertEquals(3, statuses.size());

        BudgetStatusResponse groceriesStatus =
                findByCategory(statuses, "Groceries");

        assertBigDecimalEquals(
                "500.00",
                groceriesStatus.limitAmount()
        );

        assertBigDecimalEquals(
                "450.00",
                groceriesStatus.spentAmount()
        );

        assertBigDecimalEquals(
                "50.00",
                groceriesStatus.remainingAmount()
        );

        assertBigDecimalEquals(
                "90.00",
                groceriesStatus.percentageUsed()
        );

        assertFalse(groceriesStatus.overspent());


        BudgetStatusResponse diningStatus =
                findByCategory(statuses, "Dining");

        assertBigDecimalEquals(
                "300.00",
                diningStatus.limitAmount()
        );

        assertBigDecimalEquals(
                "400.00",
                diningStatus.spentAmount()
        );

        assertBigDecimalEquals(
                "-100.00",
                diningStatus.remainingAmount()
        );

        assertBigDecimalEquals(
                "133.33",
                diningStatus.percentageUsed()
        );

        assertTrue(diningStatus.overspent());


        BudgetStatusResponse gymStatus =
                findByCategory(statuses, "Gym");

        assertBigDecimalEquals(
                "100.00",
                gymStatus.limitAmount()
        );

        assertBigDecimalEquals(
                "0",
                gymStatus.spentAmount()
        );

        assertBigDecimalEquals(
                "100.00",
                gymStatus.remainingAmount()
        );

        assertBigDecimalEquals(
                "0.00",
                gymStatus.percentageUsed()
        );

        assertFalse(gymStatus.overspent());
    }

    private BudgetStatusResponse findByCategory(
            List<BudgetStatusResponse> statuses,
            String categoryName
    ) {
        return statuses.stream()
                .filter(status ->
                        status.categoryName().equals(categoryName)
                )
                .findFirst()
                .orElseThrow();
    }

    private void assertBigDecimalEquals(
            String expected,
            BigDecimal actual
    ) {
        assertEquals(
                0,
                new BigDecimal(expected).compareTo(actual)
        );
    }
}