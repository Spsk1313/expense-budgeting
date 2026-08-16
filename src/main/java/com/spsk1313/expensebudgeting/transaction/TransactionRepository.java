package com.spsk1313.expensebudgeting.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}