package com.spsk1313.expensebudgeting.transaction;

import com.spsk1313.expensebudgeting.account.Account;
import com.spsk1313.expensebudgeting.account.AccountRepository;
import com.spsk1313.expensebudgeting.account.AccountType;
import com.spsk1313.expensebudgeting.account.exception.AccountNotFoundException;
import com.spsk1313.expensebudgeting.category.Category;
import com.spsk1313.expensebudgeting.category.CategoryRepository;
import com.spsk1313.expensebudgeting.category.CategoryType;
import com.spsk1313.expensebudgeting.category.exception.CategoryNotFoundException;
import com.spsk1313.expensebudgeting.transaction.dto.CreateExpenseRequest;
import com.spsk1313.expensebudgeting.transaction.dto.CreateTransferRequest;
import com.spsk1313.expensebudgeting.transaction.dto.TransactionResponse;
import com.spsk1313.expensebudgeting.user.User;
import com.spsk1313.expensebudgeting.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void createExpenseWithValidResourcesShouldSaveTransaction() {
        User user = createUser();
        Account account = createAccount(user, "TD CHEQUING", AccountType.CHEQUING);
        Category category = createCategory(user, "Groceries", CategoryType.EXPENSE);

        CreateExpenseRequest req = new CreateExpenseRequest(
                10L,
                20L,
                new BigDecimal("50.00"),
                "Groceries",
                LocalDate.of(2026, 8, 16)
        );

        when(userRepository.existsById(1L)).thenReturn(true);
        when(accountRepository.findByIdAndUser_Id(10L, 1L)).thenReturn(Optional.of(account));
        when(categoryRepository.findByIdAndUser_Id(20L, 1L)).thenReturn(Optional.of(category));

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = transactionService.createExpense(1L, req);

        assertEquals(TransactionType.EXPENSE, response.type());
        assertEquals(new BigDecimal("50.00"), response.amount());
        assertEquals("Groceries", response.description());

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createExpenseWithAccountOwnedByAnotherUserShouldFail() {
        CreateExpenseRequest req = new CreateExpenseRequest(
                10L,
                20L,
                new BigDecimal("50.00"),
                "Groceries",
                LocalDate.of(2026, 8, 16)
        );

        when(userRepository.existsById(1L)).thenReturn(true);
        when(accountRepository.findByIdAndUser_Id(10L, 1L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> transactionService.createExpense(1L, req));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createExpenseWithCategoryOwnedByAnotherUserShouldFail() {
        User user = createUser();
        Account account = createAccount(user, "TD Chequing", AccountType.CHEQUING);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(accountRepository.findByIdAndUser_Id(10L, 1L)).thenReturn(Optional.of(account));
        when(categoryRepository.findByIdAndUser_Id(20L, 1L)).thenReturn(Optional.empty());

        CreateExpenseRequest req = new CreateExpenseRequest(
                10L,
                20L,
                new BigDecimal("50.00"),
                "Groceries",
                LocalDate.of(2026, 8, 16)
        );

        assertThrows(CategoryNotFoundException.class, () -> transactionService.createExpense(1L, req));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransferWithDestinationAccountOwnedByAnotherUserShouldFail() {
        User user = createUser();
        Account sourceAccount = createAccount(user, "TD Chequing", AccountType.CHEQUING);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(accountRepository.findByIdAndUser_Id(10L, 1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByIdAndUser_Id(20L, 1L)).thenReturn(Optional.empty());

        CreateTransferRequest req = new CreateTransferRequest(
                10L,
                20L,
                new BigDecimal("50.00"),
                "Car Savings",
                LocalDate.of(2026, 8, 16)
        );

        assertThrows(AccountNotFoundException.class, () -> transactionService.createTransfer(1L, req));

        verify(transactionRepository, never()).save(any());
        verify(accountRepository).findByIdAndUser_Id(20L, 1L);
    }

    private User createUser() {
        return new User("Sahil", "sahil@example.com");
    }

    private Account createAccount(
            User user,
            String name,
            AccountType type
    ) {
        return new Account(
                user,
                name,
                type,
                new BigDecimal("500.00")
        );
    }

    private Category createCategory(
            User user,
            String name,
            CategoryType type
    ) {
        return new Category(user, name, type);
    }
}
