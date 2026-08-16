package com.spsk1313.expensebudgeting.recurringtransaction;

import com.spsk1313.expensebudgeting.recurringtransaction.dto.CreateRecurringExpenseRequest;
import com.spsk1313.expensebudgeting.recurringtransaction.dto.CreateRecurringIncomeRequest;
import com.spsk1313.expensebudgeting.recurringtransaction.dto.CreateRecurringTransferRequest;
import com.spsk1313.expensebudgeting.recurringtransaction.dto.RecurringTransactionResponse;
import com.spsk1313.expensebudgeting.recurringtransaction.dto.UpdateRecurringTransactionActiveRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/recurring-transactions")
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    public RecurringTransactionController(
            RecurringTransactionService recurringTransactionService
    ) {
        this.recurringTransactionService = recurringTransactionService;
    }

    @PostMapping("/expense")
    public ResponseEntity<RecurringTransactionResponse> createExpense(
            @PathVariable Long userId,
            @Valid @RequestBody CreateRecurringExpenseRequest req
    ) {
        RecurringTransactionResponse response =
                recurringTransactionService.createExpense(userId, req);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/income")
    public ResponseEntity<RecurringTransactionResponse> createIncome(
            @PathVariable Long userId,
            @Valid @RequestBody CreateRecurringIncomeRequest req
    ) {
        RecurringTransactionResponse response =
                recurringTransactionService.createIncome(userId, req);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<RecurringTransactionResponse> createTransfer(
            @PathVariable Long userId,
            @Valid @RequestBody CreateRecurringTransferRequest req
    ) {
        RecurringTransactionResponse response =
                recurringTransactionService.createTransfer(userId, req);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<RecurringTransactionResponse>>
    getRecurringTransactions(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                recurringTransactionService
                        .getRecurringTransactions(userId)
        );
    }

    @GetMapping("/{recurringTransactionId}")
    public ResponseEntity<RecurringTransactionResponse>
    getRecurringTransactionById(
            @PathVariable Long userId,
            @PathVariable Long recurringTransactionId
    ) {
        return ResponseEntity.ok(
                recurringTransactionService
                        .getRecurringTransactionById(
                                userId,
                                recurringTransactionId
                        )
        );
    }

    @PatchMapping("/{recurringTransactionId}/active")
    public ResponseEntity<RecurringTransactionResponse>
    updateActiveStatus(
            @PathVariable Long userId,
            @PathVariable Long recurringTransactionId,
            @Valid @RequestBody UpdateRecurringTransactionActiveRequest req
    ) {
        return ResponseEntity.ok(
                recurringTransactionService.updateActiveStatus(
                        userId,
                        recurringTransactionId,
                        req
                )
        );
    }
}