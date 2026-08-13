package com.spsk1313.expensebudgeting.account;

import com.spsk1313.expensebudgeting.account.dto.AccountResponse;
import com.spsk1313.expensebudgeting.account.dto.CreateAccountRequest;
import com.spsk1313.expensebudgeting.account.exception.AccountNotFoundException;
import com.spsk1313.expensebudgeting.user.User;
import com.spsk1313.expensebudgeting.user.UserRepository;
import com.spsk1313.expensebudgeting.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(
            AccountRepository accountRepository,
            UserRepository userRepository
    ) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public AccountResponse createAccount(
            Long userId,
            CreateAccountRequest req
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Account account = new Account(
                user,
                req.name().trim(),
                req.type(),
                req.openingBalance()
        );

        Account savedAccount = accountRepository.save(account);

        return toResponse(savedAccount);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccounts(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        return accountRepository.findByUser_Id(userId)
                .stream()
                .map(AccountService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(
            Long userId,
            Long accountId
    ) {
        Account account = accountRepository
                .findByIdAndUser_Id(accountId, userId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        return toResponse(account);
    }

    private static AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getUser().getId(),
                account.getName(),
                account.getType(),
                account.getOpeningBalance(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}