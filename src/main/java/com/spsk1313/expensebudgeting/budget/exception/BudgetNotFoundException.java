package com.spsk1313.expensebudgeting.budget.exception;

public class BudgetNotFoundException extends RuntimeException {
    public BudgetNotFoundException(Long id) {

        super("Budget with id " + id + " not found");
    }
}
