package com.spsk1313.expensebudgeting.report.projection;

import java.math.BigDecimal;

public interface MonthlyTotalsProjection {
    BigDecimal getTotalIncome();

    BigDecimal getTotalExpenses();
}
