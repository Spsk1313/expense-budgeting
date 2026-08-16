package com.spsk1313.expensebudgeting.recurringtransaction;

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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
class RecurringTransactionCatchUpIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private RecurringTransactionOccurrenceProcessor occurrenceProcessor;

    @Autowired
    private RecurringTransactionCatchUpService catchUpService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private RecurringTransactionRepository recurringTransactionRepository;

    @Autowired
    private RecurringTransactionOccurrenceRepository occurrenceRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private User user;
    private Account account;
    private Category category;

    @BeforeEach
    void setUp() {
        occurrenceRepository.deleteAll();
        transactionRepository.deleteAll();
        recurringTransactionRepository.deleteAll();
        categoryRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(
                new User("Sahil", "sahil@example.com")
        );

        account = accountRepository.save(
                new Account(
                        user,
                        "TD Chequing",
                        AccountType.CHEQUING,
                        new BigDecimal("1000.00")
                )
        );

        category = categoryRepository.save(
                new Category(
                        user,
                        "Groceries",
                        CategoryType.EXPENSE
                )
        );
    }

    @Test
    void processDueTransactionsWithMissedWeeklyOccurrencesShouldCatchUpAllOccurrences() {

        LocalDate startDate = LocalDate.of(2026, 8, 2);

        RecurringTransaction recurring =
                RecurringTransaction.createExpense(
                        user,
                        account,
                        category,
                        new BigDecimal("50.00"),
                        "Weekly groceries",
                        RecurrenceFrequency.WEEKLY,
                        startDate
                );

        recurring = recurringTransactionRepository.save(recurring);

        Long recurringId = recurring.getId();

        catchUpService.processDueTransactions(
                LocalDate.of(2026, 8, 16)
        );

        RecurringTransaction updated =
                recurringTransactionRepository
                        .findById(recurringId)
                        .orElseThrow();

        List<Transaction> transactions =
                transactionRepository.findAll();

        List<RecurringTransactionOccurrence> occurrences =
                occurrenceRepository.findAll();

        assertEquals(3, transactions.size());
        assertEquals(3, occurrences.size());

        List<LocalDate> transactionDates =
                transactions.stream()
                        .map(Transaction::getTransactionDate)
                        .sorted()
                        .toList();

        assertEquals(
                List.of(
                        LocalDate.of(2026, 8, 2),
                        LocalDate.of(2026, 8, 9),
                        LocalDate.of(2026, 8, 16)
                ),
                transactionDates
        );

        List<LocalDate> occurrenceDates =
                occurrences.stream()
                        .map(RecurringTransactionOccurrence::getScheduledDate)
                        .sorted()
                        .toList();

        assertEquals(
                List.of(
                        LocalDate.of(2026, 8, 2),
                        LocalDate.of(2026, 8, 9),
                        LocalDate.of(2026, 8, 16)
                ),
                occurrenceDates
        );

        assertEquals(
                LocalDate.of(2026, 8, 23),
                updated.getNextRunDate()
        );
    }

    @Test
    void processOccurrenceWhenOccurrenceAlreadyExistsShouldNotCreateDuplicateTransaction() {

        LocalDate scheduledDate = LocalDate.of(2026, 8, 16);

        RecurringTransaction recurring =
                RecurringTransaction.createExpense(
                        user,
                        account,
                        category,
                        new BigDecimal("50.00"),
                        "Weekly groceries",
                        RecurrenceFrequency.WEEKLY,
                        scheduledDate
                );

        recurring = recurringTransactionRepository.save(recurring);

        occurrenceProcessor.processOccurrence(
                recurring.getId(),
                scheduledDate
        );

        occurrenceProcessor.processOccurrence(
                recurring.getId(),
                scheduledDate
        );

        List<Transaction> transactions =
                transactionRepository.findAll();

        List<RecurringTransactionOccurrence> occurrences =
                occurrenceRepository.findAll();

        assertEquals(1, transactions.size());
        assertEquals(1, occurrences.size());

        assertEquals(
                scheduledDate,
                transactions.getFirst().getTransactionDate()
        );

        assertEquals(
                scheduledDate,
                occurrences.getFirst().getScheduledDate()
        );

        RecurringTransaction updated =
                recurringTransactionRepository
                        .findById(recurring.getId())
                        .orElseThrow();

        assertEquals(
                LocalDate.of(2026, 8, 23),
                updated.getNextRunDate()
        );
    }

    @Test
    void processDueTransactionsWithMonthlyRecurrenceOnDay31ShouldUseLastValidDayOfMonth() {

        LocalDate startDate = LocalDate.of(2026, 1, 31);

        RecurringTransaction recurring =
                RecurringTransaction.createExpense(
                        user,
                        account,
                        category,
                        new BigDecimal("50.00"),
                        "Monthly expense",
                        RecurrenceFrequency.MONTHLY,
                        startDate
                );

        recurring = recurringTransactionRepository.save(recurring);

        Long recurringId = recurring.getId();

        catchUpService.processDueTransactions(
                LocalDate.of(2026, 4, 30)
        );

        List<Transaction> transactions =
                transactionRepository.findAll();

        List<RecurringTransactionOccurrence> occurrences =
                occurrenceRepository.findAll();

        RecurringTransaction updated =
                recurringTransactionRepository
                        .findById(recurringId)
                        .orElseThrow();

        List<LocalDate> transactionDates =
                transactions.stream()
                        .map(Transaction::getTransactionDate)
                        .sorted()
                        .toList();

        List<LocalDate> occurrenceDates =
                occurrences.stream()
                        .map(RecurringTransactionOccurrence::getScheduledDate)
                        .sorted()
                        .toList();

        List<LocalDate> expectedDates = List.of(
                LocalDate.of(2026, 1, 31),
                LocalDate.of(2026, 2, 28),
                LocalDate.of(2026, 3, 31),
                LocalDate.of(2026, 4, 30)
        );

        assertEquals(4, transactions.size());
        assertEquals(4, occurrences.size());

        assertEquals(expectedDates, transactionDates);
        assertEquals(expectedDates, occurrenceDates);

        assertEquals(
                LocalDate.of(2026, 5, 31),
                updated.getNextRunDate()
        );
    }

    @Test
    void processDueTransactionsWithFutureRecurrenceShouldNotProcessOccurrence() {

        LocalDate startDate = LocalDate.of(2026, 8, 20);

        RecurringTransaction recurring =
                RecurringTransaction.createExpense(
                        user,
                        account,
                        category,
                        new BigDecimal("50.00"),
                        "Weekly groceries",
                        RecurrenceFrequency.WEEKLY,
                        startDate
                );

        recurring = recurringTransactionRepository.save(recurring);

        Long recurringId = recurring.getId();

        catchUpService.processDueTransactions(
                LocalDate.of(2026, 8, 16)
        );

        List<Transaction> transactions =
                transactionRepository.findAll();

        List<RecurringTransactionOccurrence> occurrences =
                occurrenceRepository.findAll();

        RecurringTransaction updated =
                recurringTransactionRepository
                        .findById(recurringId)
                        .orElseThrow();

        assertEquals(0, transactions.size());
        assertEquals(0, occurrences.size());

        assertEquals(
                LocalDate.of(2026, 8, 20),
                updated.getNextRunDate()
        );
    }

    @Test
    void processDueTransactionsWithInactiveRecurrenceShouldNotProcessOccurrence() {

        LocalDate startDate = LocalDate.of(2026, 8, 16);

        RecurringTransaction recurring =
                RecurringTransaction.createExpense(
                        user,
                        account,
                        category,
                        new BigDecimal("50.00"),
                        "Weekly groceries",
                        RecurrenceFrequency.WEEKLY,
                        startDate
                );

        recurring.deactivate();

        recurring = recurringTransactionRepository.save(recurring);

        Long recurringId = recurring.getId();

        catchUpService.processDueTransactions(
                LocalDate.of(2026, 8, 16)
        );

        List<Transaction> transactions =
                transactionRepository.findAll();

        List<RecurringTransactionOccurrence> occurrences =
                occurrenceRepository.findAll();

        RecurringTransaction updated =
                recurringTransactionRepository
                        .findById(recurringId)
                        .orElseThrow();

        assertEquals(0, transactions.size());
        assertEquals(0, occurrences.size());

        assertEquals(
                LocalDate.of(2026, 8, 16),
                updated.getNextRunDate()
        );
    }
}