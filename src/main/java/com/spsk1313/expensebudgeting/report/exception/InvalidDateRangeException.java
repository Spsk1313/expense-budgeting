package com.spsk1313.expensebudgeting.report.exception;

public class InvalidDateRangeException extends RuntimeException {

  public InvalidDateRangeException() {
    super("'from' date cannot be after 'to' date");
  }
}