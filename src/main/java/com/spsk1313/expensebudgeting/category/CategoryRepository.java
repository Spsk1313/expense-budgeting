package com.spsk1313.expensebudgeting.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByUser_Id(Long userId);

    Optional<Category> findByIdAndUser_Id(Long categoryId, Long userId);

    boolean existsByNameAndTypeAndUser_Id(String name, CategoryType type, Long userId);
}
