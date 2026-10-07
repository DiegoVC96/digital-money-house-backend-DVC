package com.digitalmoneyhouse.accounts.service;

import com.digitalmoneyhouse.accounts.api.AccountDtos.CreateAccountRequest;
import com.digitalmoneyhouse.accounts.domain.Account;
import com.digitalmoneyhouse.accounts.repository.AccountRepository;
import com.digitalmoneyhouse.common.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.digitalmoneyhouse.accounts.domain.Transaction;
import com.digitalmoneyhouse.accounts.domain.TransactionType;
import com.digitalmoneyhouse.accounts.repository.TransactionRepository;
import com.digitalmoneyhouse.accounts.repository.PaymentCardRepository;
import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import com.digitalmoneyhouse.accounts.api.AccountDtos.UpdateAccountRequest;
import com.digitalmoneyhouse.accounts.domain.ActivityDirection;
import com.digitalmoneyhouse.accounts.domain.ActivityAmountRange;
import com.digitalmoneyhouse.accounts.domain.PaymentCard;
import com.digitalmoneyhouse.accounts.domain.CardBrand;
import com.digitalmoneyhouse.accounts.api.AccountDtos.CreateDepositRequest;
import com.digitalmoneyhouse.common.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.security.SecureRandom;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createsAccountWithCvuAliasAndZeroBalance() {
        UUID userId = UUID.randomUUID();

        CreateAccountRequest request = new CreateAccountRequest(
            userId,
            "Ana Pérez"
        );

        when(aliasWordsProvider.randomWord(
            ArgumentMatchers.any(SecureRandom.class)
        )).thenReturn("sol", "luna", "rio");

        when(accountRepository.findByUserId(userId))
            .thenReturn(Optional.empty());

        when(accountRepository.save(ArgumentMatchers.any(Account.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var response = accountService.create(request);

        assertEquals(userId, response.userId());
        assertTrue(response.cvu().matches("\\d{22}"));
        assertEquals("sol.luna.rio", response.alias());
        assertEquals(0, response.balance().compareTo(BigDecimal.ZERO));

        verify(accountRepository).save(ArgumentMatchers.any(Account.class));
    }

    @Test
    void rejectsSecondAccountForTheSameUser() {
        UUID userId = UUID.randomUUID();

        CreateAccountRequest request = new CreateAccountRequest(
            userId,
            "Ana Pérez"
        );

        Account existingAccount = new Account(
            userId,
            "1234567890123456789012",
            "ana.dmh.123abc",
            "Ana Pérez"
        );

        when(accountRepository.findByUserId(userId))
            .thenReturn(Optional.of(existingAccount));

        assertThrows(
            ConflictException.class,
            () -> accountService.create(request)
        );

        verify(accountRepository, never())
            .save(ArgumentMatchers.any(Account.class));
    }

    @Mock
    private AliasWordsProvider aliasWordsProvider;

    @AfterEach
void clearSecurityContext() {
    SecurityContextHolder.clearContext();
}

@Test
void returnsRecentTransactionsForTheAccountOwner() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();

    Account account = new Account(
        userId,
        "1234567890123456789012",
        "sol.luna.rio",
        "Ana Pérez"
    );

    Transaction transaction = new Transaction(
        account,
        TransactionType.DEPOSIT,
        new BigDecimal("2500.00"),
        "Ingreso de dinero"
    );

    authenticateAs(userId);

    when(accountRepository.findById(accountId))
        .thenReturn(Optional.of(account));

    when(transactionRepository.findByAccount_IdOrderByCreatedAtDesc(
        eq(accountId),
        any(Pageable.class)
    )).thenReturn(List.of(transaction));

    var response = accountService.getRecentTransactions(accountId, 5);

    assertEquals(1, response.size());
    assertEquals(TransactionType.DEPOSIT, response.getFirst().type());
    assertEquals(
        0,
        response.getFirst().amount()
            .compareTo(new BigDecimal("2500.00"))
    );

    ArgumentCaptor<Pageable> pageableCaptor =
        ArgumentCaptor.forClass(Pageable.class);

    verify(transactionRepository)
        .findByAccount_IdOrderByCreatedAtDesc(
            eq(accountId),
            pageableCaptor.capture()
        );

    assertEquals(5, pageableCaptor.getValue().getPageSize());
}

@Test
void rejectsRecentTransactionsForAnotherUser() {
    UUID accountOwnerId = UUID.randomUUID();
    UUID otherUserId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();

    Account account = new Account(
        accountOwnerId,
        "1234567890123456789012",
        "sol.luna.rio",
        "Ana Pérez"
    );

    authenticateAs(otherUserId);

    when(accountRepository.findById(accountId))
        .thenReturn(Optional.of(account));

    assertThrows(
        AccessDeniedException.class,
        () -> accountService.getRecentTransactions(accountId, 5)
    );

    verifyNoInteractions(transactionRepository);
}

@Test
void updatesAliasForTheAccountOwner() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();

    Account account = new Account(
        userId,
        "1234567890123456789012",
        "sol.luna.rio",
        "Ana Pérez"
    );

    authenticateAs(userId);

    when(accountRepository.findById(accountId))
        .thenReturn(Optional.of(account));

    when(accountRepository.existsByAlias("cielo.mar.brisa"))
        .thenReturn(false);

    var response = accountService.update(
        accountId,
        new UpdateAccountRequest("cielo.mar.brisa")
    );

    assertEquals("cielo.mar.brisa", response.alias());
    assertEquals("1234567890123456789012", response.cvu());
    assertEquals(0, response.balance().compareTo(BigDecimal.ZERO));
}

@Test
void returnsActivityOrderedAndFilteredForTheAccountOwner() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    Account account = new Account(
        userId,
        "1234567890123456789012",
        "sol.luna.rio",
        "Ana Pérez"
    );
    Transaction deposit = new Transaction(
        account,
        TransactionType.DEPOSIT,
        new BigDecimal("2500.00"),
        "Ingreso de dinero"
    );
    Transaction expense = new Transaction(
        account,
        TransactionType.TRANSFER_OUT,
        new BigDecimal("500.00"),
        "Transferencia enviada"
    );

    authenticateAs(userId);
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
    when(transactionRepository.findByAccount_IdOrderByCreatedAtDesc(accountId))
        .thenReturn(List.of(deposit, expense));

    var response = accountService.getActivity(
        accountId,
        null,
        null,
        ActivityDirection.INCOME,
        null
    );

    assertEquals(1, response.size());
    assertEquals(TransactionType.DEPOSIT, response.getFirst().type());
}

@Test
void returnsNotFoundWhenActivityDoesNotBelongToTheAccount() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    UUID transactionId = UUID.randomUUID();
    Account account = new Account(
        userId,
        "1234567890123456789012",
        "sol.luna.rio",
        "Ana Pérez"
    );

    authenticateAs(userId);
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
    when(transactionRepository.findByIdAndAccount_Id(transactionId, accountId))
        .thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> accountService.getActivityDetail(accountId, transactionId)
    );
}

@Test
void depositsMoneyWithAnAssociatedCard() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    UUID cardId = UUID.randomUUID();
    Account account = new Account(
        userId,
        "1234567890123456789012",
        "sol.luna.rio",
        "Ana Pérez"
    );
    PaymentCard card = new PaymentCard(
        account,
        "fingerprint",
        "1111",
        CardBrand.VISA,
        "ANA PEREZ",
        "1028"
    );

    authenticateAs(userId);
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
    when(paymentCardRepository.findByIdAndAccount_Id(cardId, accountId))
        .thenReturn(Optional.of(card));
    when(transactionRepository.save(any(Transaction.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response = accountService.deposit(
        accountId,
        new CreateDepositRequest(cardId, new BigDecimal("1250.00"))
    );

    assertEquals(TransactionType.DEPOSIT, response.type());
    assertEquals(0, response.amount().compareTo(new BigDecimal("1250.00")));
    assertEquals(0, account.getBalance().compareTo(new BigDecimal("1250.00")));
    assertTrue(response.description().contains("1111"));
}

@Test
void rejectsDepositWithCardFromAnotherAccount() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    UUID cardId = UUID.randomUUID();
    Account account = new Account(
        userId,
        "1234567890123456789012",
        "sol.luna.rio",
        "Ana Pérez"
    );

    authenticateAs(userId);
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
    when(paymentCardRepository.findByIdAndAccount_Id(cardId, accountId))
        .thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> accountService.deposit(
            accountId,
            new CreateDepositRequest(cardId, new BigDecimal("1250.00"))
        )
    );

    verify(transactionRepository, never()).save(any(Transaction.class));
}

@Test
void filtersActivityByAmountRange() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    Account account = new Account(
        userId,
        "1234567890123456789012",
        "sol.luna.rio",
        "Ana Pérez"
    );
    Transaction lowerDeposit = new Transaction(
        account,
        TransactionType.DEPOSIT,
        new BigDecimal("500.00"),
        "Ingreso menor"
    );
    Transaction higherDeposit = new Transaction(
        account,
        TransactionType.DEPOSIT,
        new BigDecimal("1500.00"),
        "Ingreso mayor"
    );

    authenticateAs(userId);
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
    when(transactionRepository.findByAccount_IdOrderByCreatedAtDesc(accountId))
        .thenReturn(List.of(higherDeposit, lowerDeposit));

    var response = accountService.getActivity(
        accountId,
        null,
        null,
        null,
        ActivityAmountRange.ZERO_TO_1000
    );

    assertEquals(1, response.size());
    assertEquals(0, response.getFirst().amount()
        .compareTo(new BigDecimal("500.00")));
}

@Test
void rejectsActivitySearchWithAnInvalidPeriod() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    Account account = new Account(
        userId,
        "1234567890123456789012",
        "sol.luna.rio",
        "Ana Pérez"
    );

    authenticateAs(userId);
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

    assertThrows(
        IllegalArgumentException.class,
        () -> accountService.getActivity(
            accountId,
            Instant.parse("2026-10-08T00:00:00Z"),
            Instant.parse("2026-10-07T00:00:00Z"),
            null,
            null
        )
    );

    verifyNoInteractions(transactionRepository);
}

private void authenticateAs(UUID userId) {
    var authentication = new UsernamePasswordAuthenticationToken(
        userId.toString(),
        null,
        List.of(new SimpleGrantedAuthority("ROLE_USER"))
    );

    SecurityContextHolder.getContext().setAuthentication(authentication);
}
}
