package com.spsk1313.expensebudgeting.budget.exception;

public class DuplicateBudgetException extends RuntimeException {
    public DuplicateBudgetException() {
        super("Budget for this month and category already exists for the user");
    }
}
