package com.spsk1313.expensebudgeting.recurringtransaction;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {

    List<RecurringTransaction> findAllByUser_Id(Long userId);

    Optional<RecurringTransaction> findByIdAndUser_Id(
            Long recurringTransactionId,
            Long userId
    );

    List<RecurringTransaction> findAllByActiveTrueAndNextRunDateLessThanEqual(LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM RecurringTransaction r
        WHERE r.id = :id
        """)
    Optional<RecurringTransaction> findByIdForUpdate(
            @Param("id") Long id
    );
}
