package com.spsk1313.expensebudgeting.category.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(Long categoryId) {
        super("Category with id " + categoryId + " not found");
    }
}
