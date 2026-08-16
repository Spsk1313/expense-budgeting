package com.spsk1313.expensebudgeting.category.exception;

public class DuplicateCategoryException extends RuntimeException {
    public DuplicateCategoryException() {
        super("This category already exists for the current user");
    }
}
