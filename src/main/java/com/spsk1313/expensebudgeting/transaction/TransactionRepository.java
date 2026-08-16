package com.spsk1313.expensebudgeting.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    String FIND_BY_ID_AND_USER_ID = """
            SELECT t
                FROM Transaction t
                WHERE t.id = :transactionId
                AND (
                t.account.user.id = :userId
                OR t.sourceAccount.user.id = :userId
                )
            """;

    String FIND_ALL_BY_USER_ID = """
            SELECT t
            FROM Transaction t
            WHERE
            t.account.user.id = :userId
            OR t.sourceAccount.user.id = :userId
            ORDER BY t.transactionDate DESC, t.id DESC
            """;

    @Query(FIND_BY_ID_AND_USER_ID)
    Optional<Transaction> findByIdAndUserId(Long transactionId, Long userId);

    @Query(FIND_ALL_BY_USER_ID)
    List<Transaction> findAllByUserId(@Param("userId") Long userId);
}
