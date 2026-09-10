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

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createsAccountWithCvuAliasAndZeroBalance() {
        UUID userId = UUID.randomUUID();

        CreateAccountRequest request = new CreateAccountRequest(
            userId,
            "Ana Pérez"
        );

        when(accountRepository.findByUserId(userId))
            .thenReturn(Optional.empty());

        when(accountRepository.save(ArgumentMatchers.any(Account.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var response = accountService.create(request);

        assertEquals(userId, response.userId());
        assertTrue(response.cvu().matches("\\d{22}"));
        assertTrue(response.alias().contains(".dmh."));
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
}