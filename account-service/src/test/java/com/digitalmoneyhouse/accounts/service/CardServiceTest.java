package com.digitalmoneyhouse.accounts.service;

import com.digitalmoneyhouse.accounts.api.AccountDtos.CreateCardRequest;
import com.digitalmoneyhouse.accounts.domain.Account;
import com.digitalmoneyhouse.accounts.domain.CardBrand;
import com.digitalmoneyhouse.accounts.domain.PaymentCard;
import com.digitalmoneyhouse.accounts.repository.AccountRepository;
import com.digitalmoneyhouse.accounts.repository.PaymentCardRepository;
import com.digitalmoneyhouse.common.exception.ConflictException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @InjectMocks
    private CardService cardService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createsVisaCardWithoutPersistingPanOrCvc() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        Account account = accountFor(userId);

        authenticateAs(userId);

        when(accountRepository.findById(accountId))
            .thenReturn(Optional.of(account));
        when(paymentCardRepository.existsByFingerprint(anyString()))
            .thenReturn(false);
        when(paymentCardRepository.save(any(PaymentCard.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var response = cardService.create(
            accountId,
            new CreateCardRequest(
                "4111111111111111",
                "VALENTINA PRUEBA",
                "1026",
                "123"
            )
        );

        assertEquals("1111", response.lastFour());
        assertEquals(CardBrand.VISA, response.brand());
        assertEquals("VALENTINA PRUEBA", response.holderName());

        verify(paymentCardRepository).save(any(PaymentCard.class));
    }

    @Test
    void rejectsAlreadyAssociatedCard() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        authenticateAs(userId);

        when(accountRepository.findById(accountId))
            .thenReturn(Optional.of(accountFor(userId)));
        when(paymentCardRepository.existsByFingerprint(anyString()))
            .thenReturn(true);

        assertThrows(
            ConflictException.class,
            () -> cardService.create(
                accountId,
                new CreateCardRequest(
                    "4111111111111111",
                    "VALENTINA PRUEBA",
                    "1026",
                    "123"
                )
            )
        );

        verify(paymentCardRepository, never()).save(any(PaymentCard.class));
    }

    @Test
    void rejectsCardAccessForAnotherUser() {
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        authenticateAs(otherUserId);

        when(accountRepository.findById(accountId))
            .thenReturn(Optional.of(accountFor(ownerId)));

        assertThrows(
            AccessDeniedException.class,
            () -> cardService.findAllByAccount(accountId)
        );
    }

    private Account accountFor(UUID userId) {
        return new Account(
            userId,
            "1234567890123456789012",
            "sol.luna.rio",
            "Valentina Prueba"
        );
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