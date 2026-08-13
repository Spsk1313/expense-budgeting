package com.spsk1313.expensebudgeting.account.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(Long accountId) {
        super("Account with id " + accountId + " not found");
    }
}
