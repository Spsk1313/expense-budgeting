package com.spsk1313.expensebudgeting.recurringtransaction;

import com.spsk1313.expensebudgeting.account.Account;
import com.spsk1313.expensebudgeting.category.Category;
import com.spsk1313.expensebudgeting.category.CategoryType;
import com.spsk1313.expensebudgeting.transaction.TransactionType;
import com.spsk1313.expensebudgeting.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

@Entity
@Table(name = "recurring_transactions")
public class RecurringTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id")
    private Account sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id")
    private Account destinationAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecurrenceFrequency frequency;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "next_run_date", nullable = false)
    private LocalDate nextRunDate;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RecurringTransaction() {}

    private RecurringTransaction(
            User user,
            TransactionType type,
            BigDecimal amount,
            Account account,
            Account sourceAccount,
            Account destinationAccount,
            Category category,
            String description,
            RecurrenceFrequency frequency,
            LocalDate startDate
    ) {
        validateUser(user);
        validateAmount(amount);
        validateFrequency(frequency);
        validateStartDate(startDate);

        this.user = user;
        this.type = type;
        this.amount = amount;
        this.account = account;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.category = category;
        this.description = description;
        this.frequency = frequency;
        this.startDate = startDate;
        this.nextRunDate = startDate;
        this.active = true;
    }

    public static RecurringTransaction createIncome(
            User user,
            Account account,
            Category category,
            BigDecimal amount,
            String description,
            RecurrenceFrequency frequency,
            LocalDate startDate
    ) {
        validateAccount(account);
        validateCategory(category);
        validateCategoryType(category, CategoryType.INCOME);
        validateOwnership(user, account, category);

        return new RecurringTransaction(
                user,
                TransactionType.INCOME,
                amount,
                account,
                null,
                null,
                category,
                description,
                frequency,
                startDate
        );
    }

    public static RecurringTransaction createExpense(
            User user,
            Account account,
            Category category,
            BigDecimal amount,
            String description,
            RecurrenceFrequency frequency,
            LocalDate startDate
    ) {
        validateAccount(account);
        validateCategory(category);
        validateCategoryType(category, CategoryType.EXPENSE);
        validateOwnership(user, account, category);

        return new RecurringTransaction(
                user,
                TransactionType.EXPENSE,
                amount,
                account,
                null,
                null,
                category,
                description,
                frequency,
                startDate
        );
    }

    public static RecurringTransaction createTransfer(
            User user,
            Account sourceAccount,
            Account destinationAccount,
            BigDecimal amount,
            String description,
            RecurrenceFrequency frequency,
            LocalDate startDate
    ) {
        validateTransferAccounts(sourceAccount, destinationAccount);
        validateAccountOwnership(user, sourceAccount);
        validateAccountOwnership(user, destinationAccount);

        return new RecurringTransaction(
                user,
                TransactionType.TRANSFER,
                amount,
                null,
                sourceAccount,
                destinationAccount,
                null,
                description,
                frequency,
                startDate
        );
    }

    public void advanceNextRunDate() {
        if (frequency == RecurrenceFrequency.WEEKLY) {
            nextRunDate = nextRunDate.plusWeeks(1);
            return;
        }

        YearMonth nextMonth = YearMonth
                .from(nextRunDate)
                .plusMonths(1);

        int preferredDay = startDate.getDayOfMonth();

        int actualDay = Math.min(
                preferredDay,
                nextMonth.lengthOfMonth()
        );

        nextRunDate = nextMonth.atDay(actualDay);
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Account getAccount() {
        return account;
    }

    public Account getSourceAccount() {
        return sourceAccount;
    }

    public Account getDestinationAccount() {
        return destinationAccount;
    }

    public Category getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public RecurrenceFrequency getFrequency() {
        return frequency;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getNextRunDate() {
        return nextRunDate;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private static void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
    }

    private static void validateAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
    }

    private static void validateCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
    }

    private static void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than 0"
            );
        }
    }

    private static void validateFrequency(RecurrenceFrequency frequency) {
        if (frequency == null) {
            throw new IllegalArgumentException(
                    "Recurrence frequency cannot be null"
            );
        }
    }

    private static void validateStartDate(LocalDate startDate) {
        if (startDate == null) {
            throw new IllegalArgumentException(
                    "Start date cannot be null"
            );
        }
    }

    private static void validateCategoryType(
            Category category,
            CategoryType expectedType
    ) {
        if (category.getType() != expectedType) {
            throw new IllegalArgumentException(
                    expectedType == CategoryType.INCOME
                            ? "Income recurrence requires an income category"
                            : "Expense recurrence requires an expense category"
            );
        }
    }

    private static void validateTransferAccounts(
            Account sourceAccount,
            Account destinationAccount
    ) {
        validateAccount(sourceAccount);
        validateAccount(destinationAccount);

        if (Objects.equals(sourceAccount, destinationAccount)) {
            throw new IllegalArgumentException(
                    "Source and destination accounts must be different"
            );
        }
    }

    private static void validateOwnership(
            User user,
            Account account,
            Category category
    ) {
        validateUser(user);
        validateAccountOwnership(user, account);

        if (category.getUser() != user) {
            throw new IllegalArgumentException(
                    "Category must belong to the recurring transaction user"
            );
        }
    }

    private static void validateAccountOwnership(
            User user,
            Account account
    ) {
        validateUser(user);

        if (account.getUser() != user) {
            throw new IllegalArgumentException(
                    "Account must belong to the recurring transaction user"
            );
        }
    }
}