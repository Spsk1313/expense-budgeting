package com.spsk1313.expensebudgeting.transaction;

import com.spsk1313.expensebudgeting.account.Account;
import com.spsk1313.expensebudgeting.account.AccountRepository;
import com.spsk1313.expensebudgeting.account.exception.AccountNotFoundException;
import com.spsk1313.expensebudgeting.category.Category;
import com.spsk1313.expensebudgeting.category.CategoryRepository;
import com.spsk1313.expensebudgeting.category.exception.CategoryNotFoundException;
import com.spsk1313.expensebudgeting.transaction.dto.CreateExpenseRequest;
import com.spsk1313.expensebudgeting.transaction.dto.CreateIncomeRequest;
import com.spsk1313.expensebudgeting.transaction.dto.CreateTransferRequest;
import com.spsk1313.expensebudgeting.transaction.dto.TransactionResponse;
import com.spsk1313.expensebudgeting.transaction.exception.TransactionNotFoundException;
import com.spsk1313.expensebudgeting.user.UserRepository;
import com.spsk1313.expensebudgeting.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository, AccountRepository accountRepository, CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
    }

    public TransactionResponse createExpense(Long userId, CreateExpenseRequest req) {
        validateUserExists(userId);

        Account account = accountRepository.findByIdAndUser_Id(req.accountId(), userId).orElseThrow(() -> new AccountNotFoundException(req.accountId()));

        Category category = categoryRepository.findByIdAndUser_Id(req.categoryId(), userId).orElseThrow(() -> new CategoryNotFoundException(req.categoryId()));

        Transaction tx = Transaction.createExpense(account, category, req.amount(), req.description(), req.transactionDate());

        Transaction savedTx = transactionRepository.save(tx);

        return toResponse(savedTx);
    }

    public TransactionResponse createIncome(Long userId, CreateIncomeRequest req) {
        validateUserExists(userId);

        Account account = accountRepository.findByIdAndUser_Id(req.accountId(), userId).orElseThrow(() -> new AccountNotFoundException(req.accountId()));

        Category category = categoryRepository.findByIdAndUser_Id(req.categoryId(), userId).orElseThrow(() -> new CategoryNotFoundException(req.categoryId()));

        Transaction tx = Transaction.createIncome(account, category, req.amount(), req.description(), req.transactionDate());

        Transaction savedTx = transactionRepository.save(tx);

        return toResponse(savedTx);
    }

    public TransactionResponse createTransfer(Long userId, CreateTransferRequest req) {
        validateUserExists(userId);

        Account sourceAccount = accountRepository.findByIdAndUser_Id(req.sourceAccountId(), userId).orElseThrow(() -> new AccountNotFoundException(req.sourceAccountId()));

        Account destinationAccount = accountRepository.findByIdAndUser_Id(req.destinationAccountId(), userId).orElseThrow(() -> new AccountNotFoundException(req.destinationAccountId()));

        Transaction tx = Transaction.createTransfer(sourceAccount, destinationAccount, req.amount(), req.description(), req.transactionDate());

        Transaction savedTx = transactionRepository.save(tx);

        return toResponse(savedTx);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(Long userId) {
        validateUserExists(userId);

        return transactionRepository
                .findAllByUserId(userId)
                .stream()
                .map(TransactionService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long userId, Long transactionId) {
        validateUserExists(userId);

        Transaction tx = transactionRepository.findByIdAndUserId(transactionId, userId).orElseThrow(() -> new TransactionNotFoundException(transactionId));

        return toResponse(tx);
    }

    private static TransactionResponse toResponse(Transaction tx) {

        Long accountId = tx.getAccount() != null ? tx.getAccount().getId() : null;
        Long sourceAccountId = tx.getSourceAccount() != null ? tx.getSourceAccount().getId() : null;
        Long destinationAccountId = tx.getDestinationAccount() != null ? tx.getDestinationAccount().getId() : null;
        Long categoryId = tx.getCategory() != null ? tx.getCategory().getId() : null;

        return new TransactionResponse(
                tx.getId(),
                tx.getType(),
                tx.getAmount(),
                accountId,
                sourceAccountId,
                destinationAccountId,
                categoryId,
                tx.getDescription(),
                tx.getTransactionDate(),
                tx.getCreatedAt(),
                tx.getUpdatedAt()
        );
    }

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
    }
}

