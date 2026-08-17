package com.spsk1313.expensebudgeting.report;

import com.spsk1313.expensebudgeting.budget.Budget;
import com.spsk1313.expensebudgeting.budget.BudgetRepository;
import com.spsk1313.expensebudgeting.report.dto.BudgetStatusResponse;
import com.spsk1313.expensebudgeting.report.dto.CategorySpendingResponse;
import com.spsk1313.expensebudgeting.report.dto.MonthlySummaryResponse;
import com.spsk1313.expensebudgeting.report.projection.CategorySpendingProjection;
import com.spsk1313.expensebudgeting.report.projection.MonthlyTotalsProjection;
import com.spsk1313.expensebudgeting.transaction.TransactionRepository;
import com.spsk1313.expensebudgeting.user.UserRepository;
import com.spsk1313.expensebudgeting.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReportService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;

    public ReportService(UserRepository userRepository, TransactionRepository transactionRepository, BudgetRepository budgetRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
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

    @Transactional(readOnly = true)
    public List<BudgetStatusResponse> getBudgetStatus(
            Long userId,
            YearMonth month
    ) {
        validateUserExists(userId);

        List<Budget> budgets =
                budgetRepository.findAllByUser_IdAndMonth(userId, month);

        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.plusMonths(1).atDay(1);

        List<CategorySpendingProjection> categorySpending =
                transactionRepository.getCategorySpending(
                        userId,
                        startDate,
                        endDate
                );

        Map<Long, BigDecimal> spentByCategory =
                categorySpending.stream()
                        .collect(Collectors.toMap(
                                CategorySpendingProjection::getCategoryId,
                                CategorySpendingProjection::getTotalAmount
                        ));

        return budgets.stream()
                .map(budget -> {
                    BigDecimal limitAmount =
                            budget.getLimitAmount();

                    BigDecimal spentAmount =
                            spentByCategory.getOrDefault(
                                    budget.getCategory().getId(),
                                    BigDecimal.ZERO
                            );

                    BigDecimal remainingAmount =
                            limitAmount.subtract(spentAmount);

                    BigDecimal percentageUsed =
                            spentAmount
                                    .multiply(BigDecimal.valueOf(100))
                                    .divide(
                                            limitAmount,
                                            2,
                                            RoundingMode.HALF_UP
                                    );

                    boolean overspent =
                            spentAmount.compareTo(limitAmount) > 0;

                    return new BudgetStatusResponse(
                            budget.getId(),
                            budget.getCategory().getId(),
                            budget.getCategory().getName(),
                            budget.getMonth(),
                            limitAmount,
                            spentAmount,
                            remainingAmount,
                            percentageUsed,
                            overspent
                    );
                })
                .toList();
    }

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
    }
}
