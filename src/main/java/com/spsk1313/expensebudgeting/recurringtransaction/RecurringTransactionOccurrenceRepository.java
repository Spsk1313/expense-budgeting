package com.spsk1313.expensebudgeting.recurringtransaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface RecurringTransactionOccurrenceRepository
        extends JpaRepository<RecurringTransactionOccurrence, Long> {

    boolean existsByRecurringTransaction_IdAndScheduledDate(
            Long recurringTransactionId,
            LocalDate scheduledDate
    );
}