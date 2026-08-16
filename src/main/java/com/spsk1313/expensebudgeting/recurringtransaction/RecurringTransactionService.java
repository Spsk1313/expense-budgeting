package com.spsk1313.expensebudgeting.recurringtransaction;

import com.spsk1313.expensebudgeting.account.Account;
import com.spsk1313.expensebudgeting.account.AccountRepository;
import com.spsk1313.expensebudgeting.account.exception.AccountNotFoundException;
import com.spsk1313.expensebudgeting.category.Category;
import com.spsk1313.expensebudgeting.category.CategoryRepository;
import com.spsk1313.expensebudgeting.category.exception.CategoryNotFoundException;
import com.spsk1313.expensebudgeting.recurringtransaction.dto.CreateRecurringExpenseRequest;
import com.spsk1313.expensebudgeting.recurringtransaction.dto.CreateRecurringIncomeRequest;
import com.spsk1313.expensebudgeting.recurringtransaction.dto.CreateRecurringTransferRequest;
import com.spsk1313.expensebudgeting.recurringtransaction.dto.RecurringTransactionResponse;
import com.spsk1313.expensebudgeting.recurringtransaction.dto.UpdateRecurringTransactionActiveRequest;
import com.spsk1313.expensebudgeting.recurringtransaction.exception.RecurringTransactionNotFoundException;
import com.spsk1313.expensebudgeting.user.User;
import com.spsk1313.expensebudgeting.user.UserRepository;
import com.spsk1313.expensebudgeting.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RecurringTransactionService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final RecurringTransactionRepository recurringTransactionRepository;

    public RecurringTransactionService(
            UserRepository userRepository,
            AccountRepository accountRepository,
            CategoryRepository categoryRepository,
            RecurringTransactionRepository recurringTransactionRepository
    ) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.recurringTransactionRepository = recurringTransactionRepository;
    }

    public RecurringTransactionResponse createExpense(
            Long userId,
            CreateRecurringExpenseRequest req
    ) {
        User user = getUser(userId);

        Account account = getAccount(req.accountId(), userId);

        Category category = getCategory(req.categoryId(), userId);

        RecurringTransaction recurring =
                RecurringTransaction.createExpense(
                        user,
                        account,
                        category,
                        req.amount(),
                        req.description(),
                        req.frequency(),
                        req.startDate()
                );

        return toResponse(
                recurringTransactionRepository.save(recurring)
        );
    }

    public RecurringTransactionResponse createIncome(
            Long userId,
            CreateRecurringIncomeRequest req
    ) {
        User user = getUser(userId);

        Account account = getAccount(req.accountId(), userId);

        Category category = getCategory(req.categoryId(), userId);

        RecurringTransaction recurring =
                RecurringTransaction.createIncome(
                        user,
                        account,
                        category,
                        req.amount(),
                        req.description(),
                        req.frequency(),
                        req.startDate()
                );

        return toResponse(
                recurringTransactionRepository.save(recurring)
        );
    }

    public RecurringTransactionResponse createTransfer(
            Long userId,
            CreateRecurringTransferRequest req
    ) {
        User user = getUser(userId);

        Account sourceAccount =
                getAccount(req.sourceAccountId(), userId);

        Account destinationAccount =
                getAccount(req.destinationAccountId(), userId);

        RecurringTransaction recurring =
                RecurringTransaction.createTransfer(
                        user,
                        sourceAccount,
                        destinationAccount,
                        req.amount(),
                        req.description(),
                        req.frequency(),
                        req.startDate()
                );

        return toResponse(
                recurringTransactionRepository.save(recurring)
        );
    }

    @Transactional(readOnly = true)
    public List<RecurringTransactionResponse> getRecurringTransactions(
            Long userId
    ) {
        validateUserExists(userId);

        return recurringTransactionRepository
                .findAllByUser_Id(userId)
                .stream()
                .map(RecurringTransactionService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecurringTransactionResponse getRecurringTransactionById(
            Long userId,
            Long recurringTransactionId
    ) {
        validateUserExists(userId);

        RecurringTransaction recurring =
                getRecurringTransaction(
                        recurringTransactionId,
                        userId
                );

        return toResponse(recurring);
    }

    public RecurringTransactionResponse updateActiveStatus(
            Long userId,
            Long recurringTransactionId,
            UpdateRecurringTransactionActiveRequest req
    ) {
        validateUserExists(userId);

        RecurringTransaction recurring =
                getRecurringTransaction(
                        recurringTransactionId,
                        userId
                );

        if (req.active()) {
            recurring.activate();
        } else {
            recurring.deactivate();
        }

        return toResponse(recurring);
    }

    private User getUser(Long userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(userId)
                );
    }

    private Account getAccount(Long accountId, Long userId) {
        return accountRepository
                .findByIdAndUser_Id(accountId, userId)
                .orElseThrow(() ->
                        new AccountNotFoundException(accountId)
                );
    }

    private Category getCategory(Long categoryId, Long userId) {
        return categoryRepository
                .findByIdAndUser_Id(categoryId, userId)
                .orElseThrow(() ->
                        new CategoryNotFoundException(categoryId)
                );
    }

    private RecurringTransaction getRecurringTransaction(
            Long recurringTransactionId,
            Long userId
    ) {
        return recurringTransactionRepository
                .findByIdAndUser_Id(
                        recurringTransactionId,
                        userId
                )
                .orElseThrow(() ->
                        new RecurringTransactionNotFoundException(
                                recurringTransactionId
                        )
                );
    }

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
    }

    private static RecurringTransactionResponse toResponse(
            RecurringTransaction recurring
    ) {
        return new RecurringTransactionResponse(
                recurring.getId(),
                recurring.getUser().getId(),
                recurring.getType(),
                recurring.getAmount(),

                recurring.getAccount() != null
                        ? recurring.getAccount().getId()
                        : null,

                recurring.getSourceAccount() != null
                        ? recurring.getSourceAccount().getId()
                        : null,

                recurring.getDestinationAccount() != null
                        ? recurring.getDestinationAccount().getId()
                        : null,

                recurring.getCategory() != null
                        ? recurring.getCategory().getId()
                        : null,

                recurring.getDescription(),
                recurring.getFrequency(),
                recurring.getStartDate(),
                recurring.getNextRunDate(),
                recurring.isActive(),
                recurring.getCreatedAt(),
                recurring.getUpdatedAt()
        );
    }
}