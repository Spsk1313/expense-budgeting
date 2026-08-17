package com.spsk1313.expensebudgeting.export;

import com.spsk1313.expensebudgeting.account.Account;
import com.spsk1313.expensebudgeting.account.AccountRepository;
import com.spsk1313.expensebudgeting.account.AccountType;
import com.spsk1313.expensebudgeting.category.Category;
import com.spsk1313.expensebudgeting.category.CategoryRepository;
import com.spsk1313.expensebudgeting.category.CategoryType;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class CsvExportServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private CsvExportService csvExportService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void cleanDatabase() {
        transactionRepository.deleteAll();
        categoryRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void exportTransactionsShouldGenerateValidCsv() {

        User user = userRepository.save(
                new User("Sahil", "sahil@example.com")
        );

        Account account = accountRepository.save(
                new Account(
                        user,
                        "TD Chequing",
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

        transactionRepository.save(
                Transaction.createExpense(
                        account,
                        groceries,
                        new BigDecimal("50.00"),
                        "Normal groceries",
                        LocalDate.of(2026, 8, 5)
                )
        );

        transactionRepository.save(
                Transaction.createExpense(
                        account,
                        groceries,
                        new BigDecimal("75.25"),
                        "Milk, bread and vegetables",
                        LocalDate.of(2026, 8, 10)
                )
        );

        transactionRepository.save(
                Transaction.createExpense(
                        account,
                        groceries,
                        new BigDecimal("20.00"),
                        "Bought \"premium\" coffee",
                        LocalDate.of(2026, 8, 15)
                )
        );

        transactionRepository.save(
                Transaction.createExpense(
                        account,
                        groceries,
                        new BigDecimal("10.00"),
                        null,
                        LocalDate.of(2026, 8, 20)
                )
        );

        String csv = csvExportService.exportTransactions(
                user.getId(),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31)
        );

        assertTrue(csv.startsWith(
                "Date,Type,Amount,Account,Source Account," +
                        "Destination Account,Category,Description\n"
        ));

        assertTrue(csv.contains(
                "\"Normal groceries\""
        ));

        assertTrue(csv.contains(
                "\"Milk, bread and vegetables\""
        ));

        assertTrue(csv.contains(
                "\"Bought \"\"premium\"\" coffee\""
        ));

        assertTrue(csv.contains(
                "\"2026-08-20\",\"EXPENSE\",\"10.00\"," +
                        "\"TD Chequing\",\"\",\"\",\"Groceries\",\"\""
        ));

        assertEquals(
                5,
                csv.lines().count()
        );
    }
}