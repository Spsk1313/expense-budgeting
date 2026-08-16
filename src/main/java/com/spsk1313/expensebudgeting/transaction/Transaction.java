package com.spsk1313.expensebudgeting.transaction;

import com.spsk1313.expensebudgeting.account.Account;
import com.spsk1313.expensebudgeting.category.Category;
import com.spsk1313.expensebudgeting.category.CategoryType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
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

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Transaction() {
    }

    private Transaction(
            TransactionType type,
            BigDecimal amount,
            Account account,
            Account sourceAccount,
            Account destinationAccount,
            Category category,
            String description,
            LocalDate transactionDate
    ) {
        this.type = type;
        this.amount = amount;
        this.account = account;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.category = category;
        this.description = description;
        this.transactionDate = transactionDate;
    }

    public static Transaction createIncome(
            Account account,
            Category category,
            BigDecimal amount,
            String description,
            LocalDate transactionDate
    ) {
        validateAccount(account);
        validateIncomeCategory(category);
        validateAmount(amount);
        validateTransactionDate(transactionDate);

        return new Transaction(
                TransactionType.INCOME,
                amount,
                account,
                null,
                null,
                category,
                description,
                transactionDate
        );
    }

    public static Transaction createExpense(
            Account account,
            Category category,
            BigDecimal amount,
            String description,
            LocalDate transactionDate
    ) {
        validateAccount(account);
        validateExpenseCategory(category);
        validateAmount(amount);
        validateTransactionDate(transactionDate);

        return new Transaction(
                TransactionType.EXPENSE,
                amount,
                account,
                null,
                null,
                category,
                description,
                transactionDate
        );
    }

    public static Transaction createTransfer(
            Account sourceAccount,
            Account destinationAccount,
            BigDecimal amount,
            String description,
            LocalDate transactionDate
    ) {
        validateTransferAccounts(sourceAccount, destinationAccount);
        validateAmount(amount);
        validateTransactionDate(transactionDate);

        return new Transaction(
                TransactionType.TRANSFER,
                amount,
                null,
                sourceAccount,
                destinationAccount,
                null,
                description,
                transactionDate
        );
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
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

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
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

    private static void validateIncomeCategory(Category category) {
        validateCategory(category);

        if (category.getType() != CategoryType.INCOME) {
            throw new IllegalArgumentException(
                    "Income transaction requires an income category"
            );
        }
    }

    private static void validateExpenseCategory(Category category) {
        validateCategory(category);

        if (category.getType() != CategoryType.EXPENSE) {
            throw new IllegalArgumentException(
                    "Expense transaction requires an expense category"
            );
        }
    }

    private static void validateTransferAccounts(
            Account sourceAccount,
            Account destinationAccount
    ) {
        if (sourceAccount == null) {
            throw new IllegalArgumentException("Source account cannot be null");
        }

        if (destinationAccount == null) {
            throw new IllegalArgumentException("Destination account cannot be null");
        }

        if (Objects.equals(sourceAccount, destinationAccount)) {
            throw new IllegalArgumentException(
                    "Source and destination accounts must be different"
            );
        }
    }

    private static void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
    }

    private static void validateTransactionDate(LocalDate transactionDate) {
        if (transactionDate == null) {
            throw new IllegalArgumentException("Transaction date cannot be null");
        }
    }
}