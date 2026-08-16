package com.spsk1313.expensebudgeting.recurringtransaction;

import com.spsk1313.expensebudgeting.recurringtransaction.exception.RecurringTransactionNotFoundException;
import com.spsk1313.expensebudgeting.transaction.Transaction;
import com.spsk1313.expensebudgeting.transaction.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class RecurringTransactionOccurrenceProcessor {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final RecurringTransactionOccurrenceRepository occurrenceRepository;
    private final TransactionRepository transactionRepository;

    public RecurringTransactionOccurrenceProcessor(RecurringTransactionRepository recurringTransactionRepository, RecurringTransactionOccurrenceRepository occurrenceRepository, TransactionRepository transactionRepository) {
        this.recurringTransactionRepository = recurringTransactionRepository;
        this.occurrenceRepository = occurrenceRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void processOccurrence(
            Long recurringTransactionId,
            LocalDate scheduledDate
    ) {
        RecurringTransaction recurring =
                recurringTransactionRepository
                        .findByIdForUpdate(recurringTransactionId)
                        .orElseThrow(
                                () -> new RecurringTransactionNotFoundException(
                                        recurringTransactionId
                                )
                        );

        if (!recurring.isActive()) {
            return;
        }

        if (occurrenceRepository
                .existsByRecurringTransaction_IdAndScheduledDate(
                        recurringTransactionId,
                        scheduledDate
                )) {
            return;
        }

        if (!recurring.getNextRunDate().equals(scheduledDate)) {
            throw new IllegalStateException(
                    "Scheduled date does not match next run date"
            );
        }

        Transaction transaction =
                createTransaction(recurring, scheduledDate);

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        RecurringTransactionOccurrence occurrence =
                new RecurringTransactionOccurrence(
                        recurring,
                        scheduledDate,
                        savedTransaction
                );

        occurrenceRepository.save(occurrence);

        recurring.advanceNextRunDate();
    }

    private Transaction createTransaction(
            RecurringTransaction recurring, LocalDate scheduledDate
    ) {

        return switch (recurring.getType()) {
            case INCOME -> Transaction.createIncome(
                    recurring.getAccount(),
                    recurring.getCategory(),
                    recurring.getAmount(),
                    recurring.getDescription(),
                    scheduledDate
            );

            case EXPENSE -> Transaction.createExpense(
                    recurring.getAccount(),
                    recurring.getCategory(),
                    recurring.getAmount(),
                    recurring.getDescription(),
                    scheduledDate
            );

            case TRANSFER -> Transaction.createTransfer(
                    recurring.getSourceAccount(),
                    recurring.getDestinationAccount(),
                    recurring.getAmount(),
                    recurring.getDescription(),
                    scheduledDate
            );
        };
    }
}
