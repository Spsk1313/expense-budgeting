package com.spsk1313.expensebudgeting.report;

import com.spsk1313.expensebudgeting.report.dto.CategorySpendingResponse;
import com.spsk1313.expensebudgeting.report.dto.MonthlySummaryResponse;
import com.spsk1313.expensebudgeting.report.projection.MonthlyTotalsProjection;
import com.spsk1313.expensebudgeting.transaction.TransactionRepository;
import com.spsk1313.expensebudgeting.user.UserRepository;
import com.spsk1313.expensebudgeting.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional
public class ReportService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public ReportService(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public MonthlySummaryResponse getMonthlySummary(Long userId, YearMonth month) {
        validateUserExists(userId);
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.plusMonths(1).atDay(1);

        MonthlyTotalsProjection totals = transactionRepository.getMonthlyTotals(userId, startDate, endDate);

        BigDecimal totalIncome = totals.getTotalIncome();
        BigDecimal totalExpenses = totals.getTotalExpenses();
        BigDecimal netCashFlow = totalIncome.subtract(totalExpenses);

        return new MonthlySummaryResponse(
                month,
                totalIncome,
                totalExpenses,
                netCashFlow
        );

    }

    @Transactional(readOnly = true)
    public List<CategorySpendingResponse> getCategorySpending(
            Long userId,
            YearMonth month
    ) {
        validateUserExists(userId);

        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.plusMonths(1).atDay(1);

        return transactionRepository
                .getCategorySpending(userId, startDate, endDate)
                .stream()
                .map(projection -> new CategorySpendingResponse(
                        projection.getCategoryId(),
                        projection.getCategoryName(),
                        projection.getTotalAmount()
                ))
                .toList();
    }

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
    }
}
