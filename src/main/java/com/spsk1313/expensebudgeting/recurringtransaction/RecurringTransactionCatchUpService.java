package com.spsk1313.expensebudgeting.recurringtransaction;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RecurringTransactionCatchUpService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final RecurringTransactionOccurrenceProcessor occurrenceProcessor;

    public RecurringTransactionCatchUpService(
            RecurringTransactionRepository recurringTransactionRepository,
            RecurringTransactionOccurrenceProcessor occurrenceProcessor
    ) {
        this.recurringTransactionRepository = recurringTransactionRepository;
        this.occurrenceProcessor = occurrenceProcessor;
    }

    public void processDueTransactions(LocalDate today) {
        List<Long> dueIds = recurringTransactionRepository
                .findAllByActiveTrueAndNextRunDateLessThanEqual(today)
                .stream()
                .map(RecurringTransaction::getId)
                .toList();

        for (Long recurringTransactionId : dueIds) {
            catchUp(recurringTransactionId, today);
        }
    }

    private void catchUp(
            Long recurringTransactionId,
            LocalDate today
    ) {
        while (true) {
            RecurringTransaction recurring =
                    recurringTransactionRepository
                            .findById(recurringTransactionId)
                            .orElse(null);

            if (recurring == null || !recurring.isActive()) {
                return;
            }

            LocalDate nextRunDate = recurring.getNextRunDate();

            if (nextRunDate.isAfter(today)) {
                return;
            }

            occurrenceProcessor.processOccurrence(
                    recurringTransactionId,
                    nextRunDate
            );
        }
    }
}