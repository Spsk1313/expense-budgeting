package com.spsk1313.expensebudgeting.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUser_Id(Long userId);
    Optional<Account> findByIdAndUser_Id(Long id, Long userId);
}
