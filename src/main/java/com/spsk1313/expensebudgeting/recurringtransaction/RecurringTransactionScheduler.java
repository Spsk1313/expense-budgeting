package com.spsk1313.expensebudgeting.recurringtransaction;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class RecurringTransactionScheduler {

    private final RecurringTransactionCatchUpService catchUpService;

    public RecurringTransactionScheduler(
            RecurringTransactionCatchUpService catchUpService
    ) {
        this.catchUpService = catchUpService;
    }

    @Scheduled(cron = "${app.recurring-transactions.cron}")
    public void processRecurringTransactions() {
        catchUpService.processDueTransactions(LocalDate.now());
    }
}