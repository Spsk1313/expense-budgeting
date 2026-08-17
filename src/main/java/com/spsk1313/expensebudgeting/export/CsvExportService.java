package com.spsk1313.expensebudgeting.export;

import com.spsk1313.expensebudgeting.report.exception.InvalidDateRangeException;
import com.spsk1313.expensebudgeting.transaction.Transaction;
import com.spsk1313.expensebudgeting.transaction.TransactionRepository;
import com.spsk1313.expensebudgeting.user.UserRepository;
import com.spsk1313.expensebudgeting.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CsvExportService {

    private static final String CSV_HEADER =
            "Date,Type,Amount,Account,Source Account,Destination Account,Category,Description\n";

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public CsvExportService(
            UserRepository userRepository,
            TransactionRepository transactionRepository
    ) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    public String exportTransactions(
            Long userId,
            LocalDate from,
            LocalDate to
    ) {
        validateUserExists(userId);
        validateDateRange(from, to);

        LocalDate endExclusive = to.plusDays(1);

        List<Transaction> transactions =
                transactionRepository.findAllByUserIdAndDateRange(
                        userId,
                        from,
                        endExclusive
                );

        StringBuilder csv = new StringBuilder();

        csv.append(CSV_HEADER);

        for (Transaction transaction : transactions) {
            appendTransaction(csv, transaction);
        }

        return csv.toString();
    }

    private void appendTransaction(
            StringBuilder csv,
            Transaction transaction
    ) {
        String accountName =
                transaction.getAccount() == null
                        ? ""
                        : transaction.getAccount().getName();

        String sourceAccountName =
                transaction.getSourceAccount() == null
                        ? ""
                        : transaction.getSourceAccount().getName();

        String destinationAccountName =
                transaction.getDestinationAccount() == null
                        ? ""
                        : transaction.getDestinationAccount().getName();

        String categoryName =
                transaction.getCategory() == null
                        ? ""
                        : transaction.getCategory().getName();

        csv.append(escapeCsv(transaction.getTransactionDate().toString()))
                .append(",")
                .append(escapeCsv(transaction.getType().name()))
                .append(",")
                .append(escapeCsv(transaction.getAmount().toPlainString()))
                .append(",")
                .append(escapeCsv(accountName))
                .append(",")
                .append(escapeCsv(sourceAccountName))
                .append(",")
                .append(escapeCsv(destinationAccountName))
                .append(",")
                .append(escapeCsv(categoryName))
                .append(",")
                .append(escapeCsv(transaction.getDescription()))
                .append("\n");
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "\"\"";
        }

        String escaped = value.replace("\"", "\"\"");

        return "\"" + escaped + "\"";
    }

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
    }

    private void validateDateRange(
            LocalDate from,
            LocalDate to
    ) {
        if (from.isAfter(to)) {
            throw new InvalidDateRangeException();
        }
    }
}