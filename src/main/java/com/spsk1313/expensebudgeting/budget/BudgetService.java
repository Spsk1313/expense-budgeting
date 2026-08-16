package com.spsk1313.expensebudgeting.budget;


import com.spsk1313.expensebudgeting.budget.dto.BudgetResponse;
import com.spsk1313.expensebudgeting.budget.dto.CreateBudgetRequest;
import com.spsk1313.expensebudgeting.budget.dto.UpdateBudgetLimitRequest;
import com.spsk1313.expensebudgeting.budget.exception.BudgetNotFoundException;
import com.spsk1313.expensebudgeting.budget.exception.DuplicateBudgetException;
import com.spsk1313.expensebudgeting.category.Category;
import com.spsk1313.expensebudgeting.category.CategoryRepository;
import com.spsk1313.expensebudgeting.category.exception.CategoryNotFoundException;
import com.spsk1313.expensebudgeting.user.User;
import com.spsk1313.expensebudgeting.user.UserRepository;
import com.spsk1313.expensebudgeting.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BudgetService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;

    public BudgetService(UserRepository userRepository, CategoryRepository categoryRepository, BudgetRepository budgetRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.budgetRepository = budgetRepository;
    }

    public BudgetResponse createBudget(Long userId, CreateBudgetRequest req) {

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        Category category = categoryRepository.findByIdAndUser_Id(req.categoryId(), userId).orElseThrow(() -> new CategoryNotFoundException(req.categoryId()));

        if(budgetRepository.existsByUser_IdAndCategory_IdAndMonth(userId, req.categoryId(), req.month())) throw new DuplicateBudgetException();

        Budget budget = new Budget(user, category, req.month(), req.limitAmount());

        Budget savedBudget = budgetRepository.save(budget);

        return toResponse(savedBudget);
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgets(Long userId) {
        validateUserExists(userId);

        return budgetRepository
                .findAllByUser_Id(userId)
                .stream()
                .map(BudgetService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(Long userId, Long budgetId) {
        validateUserExists(userId);

        Budget budget = budgetRepository.findByIdAndUser_Id(budgetId, userId).orElseThrow(() -> new BudgetNotFoundException(budgetId));

        return toResponse(budget);
    }

    public BudgetResponse updateBudgetLimit(Long userId, Long budgetId, UpdateBudgetLimitRequest req) {
        validateUserExists(userId);

        Budget budget = budgetRepository.findByIdAndUser_Id(budgetId, userId).orElseThrow(() -> new BudgetNotFoundException(budgetId));

        budget.changeLimit(req.limitAmount());

        return toResponse(budget);
    }

    private static BudgetResponse toResponse(Budget budget) {
        return new BudgetResponse(
                budget.getId(),
                budget.getUser().getId(),
                budget.getCategory().getId(),
                budget.getCategory().getName(),
                budget.getMonth(),
                budget.getLimitAmount(),
                budget.getCreatedAt(),
                budget.getUpdatedAt()
        );
    }

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
    }
}
