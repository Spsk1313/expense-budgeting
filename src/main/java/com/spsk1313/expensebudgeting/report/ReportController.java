package com.spsk1313.expensebudgeting.report;

import com.spsk1313.expensebudgeting.report.dto.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @PathVariable Long userId,
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM")
            YearMonth month
    ) {
        MonthlySummaryResponse response =
                reportService.getMonthlySummary(userId, month);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategorySpendingResponse>> getCategorySpending(
            @PathVariable Long userId,
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM")
            YearMonth month
    ) {
        List<CategorySpendingResponse> response =
                reportService.getCategorySpending(userId, month);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/budgets")
    public ResponseEntity<List<BudgetStatusResponse>> getBudgetStatus(
            @PathVariable Long userId,
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM")
            YearMonth month
    ) {
        List<BudgetStatusResponse> response =
                reportService.getBudgetStatus(userId, month);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<DateRangeSummaryResponse> getDateRangeSummary(
            @PathVariable Long userId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {
        DateRangeSummaryResponse response =
                reportService.getDateRangeSummary(userId, from, to);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/accounts/{accountId}/balance")
    public ResponseEntity<AccountBalanceResponse> getAccountBalance(
            @PathVariable Long userId,
            @PathVariable Long accountId
    ) {
        return ResponseEntity.ok(
                reportService.getAccountBalance(userId, accountId)
        );
    }
}