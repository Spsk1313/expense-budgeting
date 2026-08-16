package com.spsk1313.expensebudgeting.recurringtransaction.exception;

public class RecurringTransactionNotFoundException extends RuntimeException {
    public RecurringTransactionNotFoundException(Long recurringTransactionId) {
        super("Recurring Transaction with " + recurringTransactionId + " not found");
    }
}
