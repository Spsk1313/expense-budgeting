package com.spsk1313.expensebudgeting.transaction;

import com.spsk1313.expensebudgeting.report.projection.MonthlyTotalsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("""
            SELECT t
            FROM Transaction t
            LEFT JOIN t.account a
            LEFT JOIN t.sourceAccount sa
            WHERE t.id = :transactionId
              AND (
                  a.user.id = :userId
                  OR sa.user.id = :userId
              )
            """)
    Optional<Transaction> findByIdAndUserId(
            @Param("transactionId") Long transactionId,
            @Param("userId") Long userId
    );

    @Query("""
            SELECT t
            FROM Transaction t
            LEFT JOIN t.account a
            LEFT JOIN t.sourceAccount sa
            WHERE a.user.id = :userId
               OR sa.user.id = :userId
            ORDER BY t.transactionDate DESC, t.id DESC
            """)
    List<Transaction> findAllByUserId(
            @Param("userId") Long userId
    );

    @Query("""
        SELECT
            COALESCE(
                SUM(
                    CASE
                        WHEN t.type = com.spsk1313.expensebudgeting.transaction.TransactionType.INCOME
                        THEN t.amount
                        ELSE 0
                    END
                ),
                0
            ) AS totalIncome,
            COALESCE(
                SUM(
                    CASE
                        WHEN t.type = com.spsk1313.expensebudgeting.transaction.TransactionType.EXPENSE
                        THEN t.amount
                        ELSE 0
                    END
                ),
                0
            ) AS totalExpenses
        FROM Transaction t
        WHERE t.account.user.id = :userId
          AND t.transactionDate >= :startDate
          AND t.transactionDate < :endDate
        """)
    MonthlyTotalsProjection getMonthlyTotals(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}