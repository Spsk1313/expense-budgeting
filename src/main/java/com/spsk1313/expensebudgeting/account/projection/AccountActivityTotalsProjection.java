package com.spsk1313.expensebudgeting.account.projection;

import java.math.BigDecimal;

public interface AccountActivityTotalsProjection {

    BigDecimal getTotalIncome();

    BigDecimal getTotalExpenses();

    BigDecimal getTotalTransfersIn();

    BigDecimal getTotalTransfersOut();
}
