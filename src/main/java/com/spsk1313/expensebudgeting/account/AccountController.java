package com.spsk1313.expensebudgeting.account;

import com.spsk1313.expensebudgeting.account.dto.AccountResponse;
import com.spsk1313.expensebudgeting.account.dto.CreateAccountRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@PathVariable Long userId, @Valid @RequestBody CreateAccountRequest req) {
        AccountResponse response = accountService.createAccount(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAccounts(@PathVariable Long userId) {
        List<AccountResponse> response = accountService.getAccounts(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long userId, @PathVariable Long accountId) {
        AccountResponse response = accountService.getAccountById(userId, accountId);
        return ResponseEntity.ok(response);
    }

}
