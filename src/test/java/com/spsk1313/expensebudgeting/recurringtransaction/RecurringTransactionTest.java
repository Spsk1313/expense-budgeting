package com.spsk1313.expensebudgeting.recurringtransaction;

import com.spsk1313.expensebudgeting.account.Account;
import com.spsk1313.expensebudgeting.account.AccountType;
import com.spsk1313.expensebudgeting.category.Category;
import com.spsk1313.expensebudgeting.category.CategoryType;
import com.spsk1313.expensebudgeting.transaction.TransactionType;
import com.spsk1313.expensebudgeting.user.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RecurringTransactionTest {

    private static final BigDecimal DEFAULT_AMOUNT =
            new BigDecimal("200.00");

    @Test
    void createExpenseWithValidDataShouldCreateRecurringExpense() {
        User user = createUser();

        Account account = createAccount(
                user,
                "TD Chequing",
                AccountType.CHEQUING
        );

        Category category = createCategory(
                user,
                "Groceries",
                CategoryType.EXPENSE
        );

        LocalDate startDate = LocalDate.of(2026, 8, 16);

        RecurringTransaction recurring =
                RecurringTransaction.createExpense(
                        user,
                        account,
                        category,
                        DEFAULT_AMOUNT,
                        "Weekly groceries",
                        RecurrenceFrequency.WEEKLY,
                        startDate
                );

        assertAll(
                () -> assertSame(user, recurring.getUser()),
                () -> assertEquals(
                        TransactionType.EXPENSE,
                        recurring.getType()
                ),
                () -> assertSame(account, recurring.getAccount()),
                () -> assertSame(category, recurring.getCategory()),
                () -> assertNull(recurring.getSourceAccount()),
                () -> assertNull(recurring.getDestinationAccount()),
                () -> assertEquals(
                        DEFAULT_AMOUNT,
                        recurring.getAmount()
                ),
                () -> assertEquals(
                        RecurrenceFrequency.WEEKLY,
                        recurring.getFrequency()
                ),
                () -> assertEquals(
                        startDate,
                        recurring.getStartDate()
                ),
                () -> assertEquals(
                        startDate,
                        recurring.getNextRunDate()
                ),
                () -> assertTrue(recurring.isActive())
        );
    }

    @Test
    void createIncomeWithValidDataShouldCreateRecurringIncome() {
        User user = createUser();

        Account account = createAccount(
                user,
                "TD Chequing",
                AccountType.CHEQUING
        );

        Category category = createCategory(
                user,
                "Salary",
                CategoryType.INCOME
        );

        LocalDate startDate = LocalDate.of(2026, 8, 16);

        RecurringTransaction recurring =
                RecurringTransaction.createIncome(
                        user,
                        account,
                        category,
                        DEFAULT_AMOUNT,
                        "Salary",
                        RecurrenceFrequency.MONTHLY,
                        startDate
                );

        assertAll(
                () -> assertEquals(
                        TransactionType.INCOME,
                        recurring.getType()
                ),
                () -> assertSame(account, recurring.getAccount()),
                () -> assertSame(category, recurring.getCategory()),
                () -> assertNull(recurring.getSourceAccount()),
                () -> assertNull(recurring.getDestinationAccount()),
                () -> assertEquals(
                        startDate,
                        recurring.getNextRunDate()
                ),
                () -> assertTrue(recurring.isActive())
        );
    }

    @Test
    void createTransferWithValidDataShouldCreateRecurringTransfer() {
        User user = createUser();

        Account sourceAccount = createAccount(
                user,
                "TD Chequing",
                AccountType.CHEQUING
        );

        Account destinationAccount = createAccount(
                user,
                "TD Savings",
                AccountType.SAVINGS
        );

        LocalDate startDate = LocalDate.of(2026, 8, 16);

        RecurringTransaction recurring =
                RecurringTransaction.createTransfer(
                        user,
                        sourceAccount,
                        destinationAccount,
                        DEFAULT_AMOUNT,
                        "Monthly savings",
                        RecurrenceFrequency.MONTHLY,
                        startDate
                );

        assertAll(
                () -> assertEquals(
                        TransactionType.TRANSFER,
                        recurring.getType()
                ),
                () -> assertNull(recurring.getAccount()),
                () -> assertNull(recurring.getCategory()),
                () -> assertSame(
                        sourceAccount,
                        recurring.getSourceAccount()
                ),
                () -> assertSame(
                        destinationAccount,
                        recurring.getDestinationAccount()
                ),
                () -> assertEquals(
                        startDate,
                        recurring.getNextRunDate()
                )
        );
    }

    @Test
    void advanceNextRunDateForWeeklyRecurrenceShouldAddOneWeek() {
        RecurringTransaction recurring =
                createRecurringExpense(
                        RecurrenceFrequency.WEEKLY,
                        LocalDate.of(2026, 8, 16)
                );

        recurring.advanceNextRunDate();

        assertEquals(
                LocalDate.of(2026, 8, 23),
                recurring.getNextRunDate()
        );
    }

    @Test
    void advanceNextRunDateForMonthlyRecurrenceShouldAdvanceToSameDay() {
        RecurringTransaction recurring =
                createRecurringExpense(
                        RecurrenceFrequency.MONTHLY,
                        LocalDate.of(2026, 8, 15)
                );

        recurring.advanceNextRunDate();

        assertEquals(
                LocalDate.of(2026, 9, 15),
                recurring.getNextRunDate()
        );
    }

    @Test
    void advanceNextRunDateFrom31stShouldUseLastValidDayWithoutDrifting() {
        RecurringTransaction recurring =
                createRecurringExpense(
                        RecurrenceFrequency.MONTHLY,
                        LocalDate.of(2026, 1, 31)
                );

        recurring.advanceNextRunDate();

        assertEquals(
                LocalDate.of(2026, 2, 28),
                recurring.getNextRunDate()
        );

        recurring.advanceNextRunDate();

        assertEquals(
                LocalDate.of(2026, 3, 31),
                recurring.getNextRunDate()
        );
    }

    @Test
    void advanceNextRunDateFrom31stInLeapYearShouldUseFebruary29() {
        RecurringTransaction recurring =
                createRecurringExpense(
                        RecurrenceFrequency.MONTHLY,
                        LocalDate.of(2028, 1, 31)
                );

        recurring.advanceNextRunDate();

        assertEquals(
                LocalDate.of(2028, 2, 29),
                recurring.getNextRunDate()
        );

        recurring.advanceNextRunDate();

        assertEquals(
                LocalDate.of(2028, 3, 31),
                recurring.getNextRunDate()
        );
    }

    @Test
    void createIncomeWithExpenseCategoryShouldFail() {
        User user = createUser();
        Account account = createAccount(
                user,
                "TD Chequing",
                AccountType.CHEQUING
        );
        Category category = createCategory(
                user,
                "Groceries",
                CategoryType.EXPENSE
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> RecurringTransaction.createIncome(
                        user,
                        account,
                        category,
                        DEFAULT_AMOUNT,
                        null,
                        RecurrenceFrequency.MONTHLY,
                        LocalDate.of(2026, 8, 16)
                )
        );
    }

    @Test
    void createExpenseWithIncomeCategoryShouldFail() {
        User user = createUser();
        Account account = createAccount(
                user,
                "TD Chequing",
                AccountType.CHEQUING
        );
        Category category = createCategory(
                user,
                "Salary",
                CategoryType.INCOME
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> RecurringTransaction.createExpense(
                        user,
                        account,
                        category,
                        DEFAULT_AMOUNT,
                        null,
                        RecurrenceFrequency.MONTHLY,
                        LocalDate.of(2026, 8, 16)
                )
        );
    }

    @Test
    void createTransferWithSameAccountsShouldFail() {
        User user = createUser();

        Account account = createAccount(
                user,
                "TD Chequing",
                AccountType.CHEQUING
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> RecurringTransaction.createTransfer(
                        user,
                        account,
                        account,
                        DEFAULT_AMOUNT,
                        null,
                        RecurrenceFrequency.MONTHLY,
                        LocalDate.of(2026, 8, 16)
                )
        );
    }

    @Test
    void createRecurringTransactionWithZeroAmountShouldFail() {
        User user = createUser();
        Account account = createAccount(
                user,
                "TD Chequing",
                AccountType.CHEQUING
        );
        Category category = createCategory(
                user,
                "Groceries",
                CategoryType.EXPENSE
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> RecurringTransaction.createExpense(
                        user,
                        account,
                        category,
                        BigDecimal.ZERO,
                        null,
                        RecurrenceFrequency.MONTHLY,
                        LocalDate.of(2026, 8, 16)
                )
        );
    }

    private RecurringTransaction createRecurringExpense(
            RecurrenceFrequency frequency,
            LocalDate startDate
    ) {
        User user = createUser();

        Account account = createAccount(
                user,
                "TD Chequing",
                AccountType.CHEQUING
        );

        Category category = createCategory(
                user,
                "Groceries",
                CategoryType.EXPENSE
        );

        return RecurringTransaction.createExpense(
                user,
                account,
                category,
                DEFAULT_AMOUNT,
                "Groceries",
                frequency,
                startDate
        );
    }

    private User createUser() {
        return new User(
                "Sahil",
                "sahil@example.com"
        );
    }

    private Account createAccount(
            User user,
            String name,
            AccountType type
    ) {
        return new Account(
                user,
                name,
                type,
                new BigDecimal("500.00")
        );
    }

    private Category createCategory(
            User user,
            String name,
            CategoryType type
    ) {
        return new Category(
                user,
                name,
                type
        );
    }
}