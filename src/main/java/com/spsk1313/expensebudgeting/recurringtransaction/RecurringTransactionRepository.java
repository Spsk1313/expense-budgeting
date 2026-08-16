package com.spsk1313.expensebudgeting.recurringtransaction;

import org.springframework.data.jpa.repository.JpaRepository;

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
}
