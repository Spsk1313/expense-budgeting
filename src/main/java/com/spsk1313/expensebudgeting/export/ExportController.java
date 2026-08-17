package com.spsk1313.expensebudgeting.export;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/users/{userId}/exports")
public class ExportController {

    private final CsvExportService csvExportService;

    public ExportController(CsvExportService csvExportService) {
        this.csvExportService = csvExportService;
    }

    @GetMapping("/transactions")
    public ResponseEntity<byte[]> exportTransactions(
            @PathVariable Long userId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {
        String csv = csvExportService.exportTransactions(
                userId,
                from,
                to
        );

        byte[] csvBytes =
                csv.getBytes(StandardCharsets.UTF_8);

        String filename =
                "transactions-" + from + "-to-" + to + ".csv";

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                "text/csv;charset=UTF-8"
                        )
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\""
                )
                .body(csvBytes);
    }
}