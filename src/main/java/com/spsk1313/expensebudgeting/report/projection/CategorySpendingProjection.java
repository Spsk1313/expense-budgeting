package com.spsk1313.expensebudgeting.report.projection;

import java.math.BigDecimal;

public interface CategorySpendingProjection {

    Long getCategoryId();

    String getCategoryName();

    BigDecimal getTotalAmount();
}
