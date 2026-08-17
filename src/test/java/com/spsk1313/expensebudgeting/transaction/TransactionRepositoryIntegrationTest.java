package com.spsk1313.expensebudgeting.transaction;

import com.spsk1313.expensebudgeting.account.Account;
import com.spsk1313.expensebudgeting.account.AccountRepository;
import com.spsk1313.expensebudgeting.account.AccountType;
import com.spsk1313.expensebudgeting.category.Category;
import com.spsk1313.expensebudgeting.category.CategoryRepository;
import com.spsk1313.expensebudgeting.category.CategoryType;
import com.spsk1313.expensebudgeting.report.projection.MonthlyTotalsProjection;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
class TransactionRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        categoryRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void getTotalsByDateRangeShouldAggregateIncomeAndExpensesWithinDateRange() {

        User user = userRepository.save(
                new User("Sahil", "sahil@example.com")
        );

        Account chequing = accountRepository.save(
                new Account(
                        user,
                        "Chequing",
                        AccountType.CHEQUING,
                        new BigDecimal("1000.00")
                )
        );

        Account savings = accountRepository.save(
                new Account(
                        user,
                        "Savings",
                        AccountType.SAVINGS,
                        new BigDecimal("500.00")
                )
        );

        Category salary = categoryRepository.save(
                new Category(
                        user,
                        "Salary",
                        CategoryType.INCOME
                )
        );

        Category groceries = categoryRepository.save(
                new Category(
                        user,
                        "Groceries",
                        CategoryType.EXPENSE
                )
        );

        transactionRepository.save(
                Transaction.createIncome(
                        chequing,
                        salary,
                        new BigDecimal("2500.00"),
                        "Salary",
                        LocalDate.of(2026, 8, 1)
                )
        );

        transactionRepository.save(
                Transaction.createIncome(
                        chequing,
                        salary,
                        new BigDecimal("500.00"),
                        "Freelance",
                        LocalDate.of(2026, 8, 15)
                )
        );

        transactionRepository.save(
                Transaction.createExpense(
                        chequing,
                        groceries,
                        new BigDecimal("200.00"),
                        "Groceries",
                        LocalDate.of(2026, 8, 10)
                )
        );

        transactionRepository.save(
                Transaction.createExpense(
                        chequing,
                        groceries,
                        new BigDecimal("75.00"),
                        "More groceries",
                        LocalDate.of(2026, 8, 31)
                )
        );

        transactionRepository.save(
                Transaction.createTransfer(
                        chequing,
                        savings,
                        new BigDecimal("300.00"),
                        "Move to savings",
                        LocalDate.of(2026, 8, 20)
                )
        );

        transactionRepository.save(
                Transaction.createExpense(
                        chequing,
                        groceries,
                        new BigDecimal("999.00"),
                        "July expense",
                        LocalDate.of(2026, 7, 31)
                )
        );

        MonthlyTotalsProjection totals =
                transactionRepository.getTotalsByDateRange(
                        user.getId(),
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 9, 1)
                );

        assertEquals(
                0,
                new BigDecimal("3000.00")
                        .compareTo(totals.getTotalIncome())
        );

        assertEquals(
                0,
                new BigDecimal("275.00")
                        .compareTo(totals.getTotalExpenses())
        );
    }

    @Test
    void getTotalsByDateRangeWithNoTransactionsShouldReturnZeros() {

        User user = userRepository.save(
                new User("Sahil", "sahil@example.com")
        );

        MonthlyTotalsProjection totals =
                transactionRepository.getTotalsByDateRange(
                        user.getId(),
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 10, 1)
                );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(totals.getTotalIncome())
        );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(totals.getTotalExpenses())
        );
    }
}
