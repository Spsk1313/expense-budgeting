package com.spsk1313.expensebudgeting.user.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() {
        super("This email is already in use");
    }
}
