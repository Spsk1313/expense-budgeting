package com.spsk1313.expensebudgeting.budget;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findAllByUser_Id(Long userId);

    Optional<Budget> findByIdAndUser_Id(Long budgetId, Long userId);

    boolean existsByUser_IdAndCategory_IdAndMonth(
            Long userId,
            Long categoryId,
            YearMonth month
    );
}