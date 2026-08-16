package com.spsk1313.expensebudgeting.budget;

import com.spsk1313.expensebudgeting.budget.dto.BudgetResponse;
import com.spsk1313.expensebudgeting.budget.dto.CreateBudgetRequest;
import com.spsk1313.expensebudgeting.budget.dto.UpdateBudgetLimitRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(@PathVariable Long userId, @Valid @RequestBody CreateBudgetRequest req) {
        BudgetResponse response = budgetService.createBudget(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getBudgets(@PathVariable Long userId) {
        List<BudgetResponse> response = budgetService.getBudgets(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{budgetId}")
    public ResponseEntity<BudgetResponse> getBudgetById(@PathVariable Long userId, @PathVariable Long budgetId) {
        BudgetResponse response = budgetService.getBudgetById(userId, budgetId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{budgetId}/limit")
    public ResponseEntity<BudgetResponse> updateBudgetLimit(@PathVariable Long userId, @PathVariable Long budgetId, @Valid @RequestBody UpdateBudgetLimitRequest req) {
        BudgetResponse response = budgetService.updateBudgetLimit(userId, budgetId, req);
        return ResponseEntity.ok(response);
    }
}
