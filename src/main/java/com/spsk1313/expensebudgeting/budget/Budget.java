package com.spsk1313.expensebudgeting.budget;

import com.spsk1313.expensebudgeting.category.Category;
import com.spsk1313.expensebudgeting.category.CategoryType;
import com.spsk1313.expensebudgeting.common.persistence.YearMonthConverter;
import com.spsk1313.expensebudgeting.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;

@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Convert(converter = YearMonthConverter.class)
    @Column(name = "month", nullable = false)
    private YearMonth month;

    @Column(name = "limit_amount", nullable = false)
    private BigDecimal limitAmount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    protected Budget() {}

    public Budget(User user, Category category, YearMonth month, BigDecimal limitAmount) {
        validateUser(user);
        validateCategory(category);
        validateMonth(month);
        validateLimitAmount(limitAmount);
        validateCategoryOwnership(user, category);
        validateExpenseCategory(category);

        this.user = user;
        this.category = category;
        this.month = month;
        this.limitAmount = limitAmount;
    }

    public void changeLimit(BigDecimal limitAmount) {
        validateLimitAmount(limitAmount);
        this.limitAmount = limitAmount;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Category getCategory() {
        return category;
    }

    public YearMonth getMonth() {
        return month;
    }

    public BigDecimal getLimitAmount() {
        return limitAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private static void validateUser(User user) {
        if(user == null) throw new IllegalArgumentException("User cannot be null");
    }

    private static void validateCategory(Category category) {
        if(category == null) throw new IllegalArgumentException("Category cannot be null");
    }

    private static void validateExpenseCategory(Category category) {
        if(category.getType() != CategoryType.EXPENSE) throw new IllegalArgumentException("Budget requires an expense category");
    }

    private static void validateCategoryOwnership(User user, Category category) {
        User categoryOwner = category.getUser();

        if (user == categoryOwner) {
            return;
        }

        if (user.getId() == null
                || categoryOwner.getId() == null
                || !user.getId().equals(categoryOwner.getId())) {
            throw new IllegalArgumentException(
                    "Category must belong to the budget user"
            );
        }
    }

    private static void validateMonth(YearMonth month) {
        if(month == null) throw new IllegalArgumentException("Budget month cannot be null");
    }

    private static void validateLimitAmount(BigDecimal limitAmount) {
        if(limitAmount == null) throw new IllegalArgumentException("Limit amount cannot be null");
        if(limitAmount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Budget limit must be greater than 0");
    }
}
