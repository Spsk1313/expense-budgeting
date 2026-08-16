package com.spsk1313.expensebudgeting.transaction.exception;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(Long transactionId) {
        super("Transaction with id " + transactionId + " not found");
    }
}
