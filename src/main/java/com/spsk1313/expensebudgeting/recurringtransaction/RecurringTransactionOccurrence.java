package com.spsk1313.expensebudgeting.recurringtransaction;

import com.spsk1313.expensebudgeting.transaction.Transaction;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "recurring_transaction_occurrences")
public class RecurringTransactionOccurrence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recurring_transaction_id", nullable = false)
    private RecurringTransaction recurringTransaction;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", unique = true)
    private Transaction transaction;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected RecurringTransactionOccurrence() {}

    public RecurringTransactionOccurrence(
            RecurringTransaction recurringTransaction,
            LocalDate scheduledDate,
            Transaction transaction
    ) {
        if (recurringTransaction == null) {
            throw new IllegalArgumentException(
                    "Recurring transaction cannot be null"
            );
        }

        if (scheduledDate == null) {
            throw new IllegalArgumentException(
                    "Scheduled date cannot be null"
            );
        }

        if (transaction == null) {
            throw new IllegalArgumentException(
                    "Transaction cannot be null"
            );
        }

        this.recurringTransaction = recurringTransaction;
        this.scheduledDate = scheduledDate;
        this.transaction = transaction;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public RecurringTransaction getRecurringTransaction() {
        return recurringTransaction;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}