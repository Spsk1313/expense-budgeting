package com.spsk1313.expensebudgeting.transaction;

import com.spsk1313.expensebudgeting.transaction.dto.CreateExpenseRequest;
import com.spsk1313.expensebudgeting.transaction.dto.CreateIncomeRequest;
import com.spsk1313.expensebudgeting.transaction.dto.CreateTransferRequest;
import com.spsk1313.expensebudgeting.transaction.dto.TransactionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/expense")
    public ResponseEntity<TransactionResponse> createExpense(@PathVariable Long userId, @Valid @RequestBody CreateExpenseRequest req) {
        TransactionResponse response = transactionService.createExpense(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/income")
    public ResponseEntity<TransactionResponse> createIncome(@PathVariable Long userId, @Valid @RequestBody CreateIncomeRequest req) {
        TransactionResponse response = transactionService.createIncome(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> createTransfer(@PathVariable Long userId, @Valid @RequestBody CreateTransferRequest req) {
        TransactionResponse response = transactionService.createTransfer(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getTransactions(@PathVariable Long userId) {
        List<TransactionResponse> response = transactionService.getTransactions(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long userId, @PathVariable Long transactionId) {
        TransactionResponse response = transactionService.getTransactionById(userId, transactionId);
        return ResponseEntity.ok(response);
    }

}
