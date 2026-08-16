package com.spsk1313.expensebudgeting.category;

import com.spsk1313.expensebudgeting.user.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "categories")
public class Category {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_TYPE_LENGTH = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = MAX_TYPE_LENGTH)
    private CategoryType type;

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

    protected Category() {}

    public Category(User user, String name, CategoryType type) {
        validateUser(user);
        validateName(name);
        validateType(type);

        this.user = user;
        this.name = name;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public CategoryType getType() {
        return type;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private static void validateName(String name) {
        if(name == null || name.isBlank()) throw new IllegalArgumentException("Name cannot be null or blank");
        if(name.length() > MAX_NAME_LENGTH) throw new IllegalArgumentException("Name cannot have more than " + MAX_NAME_LENGTH + " characters");
    }

    private static void validateUser(User user) {
        if(user == null) throw new IllegalArgumentException("User cannot be null");
    }

    private static void validateType(CategoryType type) {
        if(type == null) throw new IllegalArgumentException("Type cannot be null");
    }
}
