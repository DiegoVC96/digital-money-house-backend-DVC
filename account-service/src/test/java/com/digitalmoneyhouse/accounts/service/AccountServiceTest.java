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
import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import com.digitalmoneyhouse.accounts.api.AccountDtos.UpdateAccountRequest;

import java.math.BigDecimal;
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

private void authenticateAs(UUID userId) {
    var authentication = new UsernamePasswordAuthenticationToken(
        userId.toString(),
        null,
        List.of(new SimpleGrantedAuthority("ROLE_USER"))
    );

    SecurityContextHolder.getContext().setAuthentication(authentication);
}
}