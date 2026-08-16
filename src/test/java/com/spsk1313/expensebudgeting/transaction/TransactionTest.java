package com.spsk1313.expensebudgeting.transaction;

import com.spsk1313.expensebudgeting.account.Account;
import com.spsk1313.expensebudgeting.account.AccountType;
import com.spsk1313.expensebudgeting.category.Category;
import com.spsk1313.expensebudgeting.category.CategoryType;
import com.spsk1313.expensebudgeting.user.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("200.00");
    private static final LocalDate DEFAULT_DATE = LocalDate.of(2026, 8, 16);

    @Test
    void createExpenseShouldCreateValidExpense() {
        User user = createUser();
        Account account = createAccount(user, "TD Savings", AccountType.SAVINGS);
        Category category = createCategory(user, "Groceries", CategoryType.EXPENSE);

        Transaction tx = Transaction.createExpense(
                account,
                category,
                DEFAULT_AMOUNT,
                null,
                DEFAULT_DATE
        );

        assertAll(
                () -> assertEquals(TransactionType.EXPENSE, tx.getType()),
                () -> assertSame(account, tx.getAccount()),
                () -> assertSame(category, tx.getCategory()),
                () -> assertEquals(DEFAULT_AMOUNT, tx.getAmount()),
                () -> assertEquals(DEFAULT_DATE, tx.getTransactionDate()),
                () -> assertNull(tx.getSourceAccount()),
                () -> assertNull(tx.getDestinationAccount())
        );
    }

    @Test
    void createIncomeShouldCreateValidIncome() {
        User user = createUser();
        Account account = createAccount(user, "Scotia Chequing", AccountType.CHEQUING);
        Category category = createCategory(user, "Salary", CategoryType.INCOME);

        Transaction tx = Transaction.createIncome(
                account,
                category,
                DEFAULT_AMOUNT,
                null,
                DEFAULT_DATE
        );

        assertAll(
                () -> assertEquals(TransactionType.INCOME, tx.getType()),
                () -> assertSame(account, tx.getAccount()),
                () -> assertSame(category, tx.getCategory()),
                () -> assertEquals(DEFAULT_AMOUNT, tx.getAmount()),
                () -> assertEquals(DEFAULT_DATE, tx.getTransactionDate()),
                () -> assertNull(tx.getSourceAccount()),
                () -> assertNull(tx.getDestinationAccount())
        );
    }

    @Test
    void createTransferShouldCreateValidTransfer() {
        User user = createUser();

        Account sourceAccount = createAccount(
                user,
                "Scotia Chequing",
                AccountType.CHEQUING
        );

        Account destinationAccount = createAccount(
                user,
                "TD Savings",
                AccountType.SAVINGS
        );

        Transaction tx = Transaction.createTransfer(
                sourceAccount,
                destinationAccount,
                DEFAULT_AMOUNT,
                null,
                DEFAULT_DATE
        );

        assertAll(
                () -> assertEquals(TransactionType.TRANSFER, tx.getType()),
                () -> assertSame(sourceAccount, tx.getSourceAccount()),
                () -> assertSame(destinationAccount, tx.getDestinationAccount()),
                () -> assertEquals(DEFAULT_AMOUNT, tx.getAmount()),
                () -> assertEquals(DEFAULT_DATE, tx.getTransactionDate()),
                () -> assertNull(tx.getAccount()),
                () -> assertNull(tx.getCategory())
        );
    }

    @Test
    void createTransactionShouldRejectZeroAmount() {
        User user = createUser();
        Account account = createAccount(user, "Scotia Chequing", AccountType.CHEQUING);
        Category category = createCategory(user, "Salary", CategoryType.INCOME);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Transaction.createIncome(
                        account,
                        category,
                        BigDecimal.ZERO,
                        null,
                        DEFAULT_DATE
                )
        );

        assertEquals("Amount must be greater than 0", ex.getMessage());
    }

    @Test
    void createTransactionShouldRejectNegativeAmount() {
        User user = createUser();
        Account account = createAccount(user, "Scotia Chequing", AccountType.CHEQUING);
        Category category = createCategory(user, "Salary", CategoryType.INCOME);

        assertThrows(
                IllegalArgumentException.class,
                () -> Transaction.createIncome(
                        account,
                        category,
                        new BigDecimal("-100.00"),
                        null,
                        DEFAULT_DATE
                )
        );
    }

    @Test
    void createTransferShouldRejectSameSourceAndDestination() {
        User user = createUser();
        Account account = createAccount(user, "Scotia Chequing", AccountType.CHEQUING);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Transaction.createTransfer(
                        account,
                        account,
                        DEFAULT_AMOUNT,
                        null,
                        DEFAULT_DATE
                )
        );

        assertEquals(
                "Source and destination accounts must be different",
                ex.getMessage()
        );
    }

    @Test
    void createIncomeShouldRejectExpenseCategory() {
        User user = createUser();
        Account account = createAccount(user, "Scotia Chequing", AccountType.CHEQUING);
        Category category = createCategory(user, "Groceries", CategoryType.EXPENSE);

        assertThrows(
                IllegalArgumentException.class,
                () -> Transaction.createIncome(
                        account,
                        category,
                        DEFAULT_AMOUNT,
                        null,
                        DEFAULT_DATE
                )
        );
    }

    @Test
    void createExpenseShouldRejectIncomeCategory() {
        User user = createUser();
        Account account = createAccount(user, "Scotia Chequing", AccountType.CHEQUING);
        Category category = createCategory(user, "Salary", CategoryType.INCOME);

        assertThrows(
                IllegalArgumentException.class,
                () -> Transaction.createExpense(
                        account,
                        category,
                        DEFAULT_AMOUNT,
                        null,
                        DEFAULT_DATE
                )
        );
    }

    private User createUser() {
        return new User("Sahil", "sahil@example.com");
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
        return new Category(user, name, type);
    }
}