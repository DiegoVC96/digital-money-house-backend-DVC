package com.digitalmoneyhouse.accounts.service;

import com.digitalmoneyhouse.accounts.api.AccountDtos.CardResponse;
import com.digitalmoneyhouse.accounts.api.AccountDtos.CreateCardRequest;
import com.digitalmoneyhouse.accounts.domain.Account;
import com.digitalmoneyhouse.accounts.domain.CardBrand;
import com.digitalmoneyhouse.accounts.domain.PaymentCard;
import com.digitalmoneyhouse.accounts.repository.AccountRepository;
import com.digitalmoneyhouse.accounts.repository.PaymentCardRepository;
import com.digitalmoneyhouse.common.exception.ConflictException;
import com.digitalmoneyhouse.common.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;
import java.util.HexFormat;

@Service
@Transactional
public class CardService {

    private final AccountRepository accountRepository;
    private final PaymentCardRepository paymentCardRepository;

    public CardService(
        AccountRepository accountRepository,
        PaymentCardRepository paymentCardRepository
    ) {
        this.accountRepository = accountRepository;
        this.paymentCardRepository = paymentCardRepository;
    }

    public CardResponse create(
        UUID accountId,
        CreateCardRequest request
    ) {
        Account account = findAuthorizedAccount(accountId);
        String number = request.number().replaceAll("\\s", "");

        if (!isValidCardNumber(number)) {
            throw new IllegalArgumentException("El número de tarjeta no es válido");
        }

        String fingerprint = fingerprint(number);

        if (paymentCardRepository.existsByFingerprint(fingerprint)) {
            throw new ConflictException(
                "La tarjeta ya se encuentra asociada a una cuenta"
            );
        }

        PaymentCard card = new PaymentCard(
            account,
            fingerprint,
            number.substring(number.length() - 4),
            detectBrand(number),
            request.name(),
            request.expiration()
        );

        return toResponse(paymentCardRepository.save(card));
    }

    @Transactional(readOnly = true)
    public List<CardResponse> findAllByAccount(UUID accountId) {
        findAuthorizedAccount(accountId);

        return paymentCardRepository.findByAccount_Id(accountId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public CardResponse findById(UUID accountId, UUID cardId) {
        findAuthorizedAccount(accountId);

        PaymentCard card = paymentCardRepository
            .findByIdAndAccount_Id(cardId, accountId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Tarjeta no encontrada")
            );

        return toResponse(card);
    }

    public void delete(UUID accountId, UUID cardId) {
        findAuthorizedAccount(accountId);

        PaymentCard card = paymentCardRepository
            .findByIdAndAccount_Id(cardId, accountId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Tarjeta no encontrada")
            );

        paymentCardRepository.delete(card);
    }

    private Account findAuthorizedAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Cuenta no encontrada")
            );

        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(authority ->
                authority.getAuthority().equals("ROLE_ADMIN")
            );

        boolean isOwner = account.getUserId().toString()
            .equals(authentication.getName());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(
                "No tenés permiso para acceder a esta cuenta"
            );
        }

        return account;
    }

    private boolean isValidCardNumber(String number) {
        int sum = 0;
        boolean doubleDigit = false;

        for (int index = number.length() - 1; index >= 0; index--) {
            int digit = Character.digit(number.charAt(index), 10);

            if (digit < 0) {
                return false;
            }

            if (doubleDigit) {
                digit *= 2;

                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            doubleDigit = !doubleDigit;
        }

        return sum % 10 == 0;
    }

    private CardBrand detectBrand(String number) {
        if (number.startsWith("4")) {
            return CardBrand.VISA;
        }

        if (
            number.startsWith("51")
                || number.startsWith("52")
                || number.startsWith("53")
                || number.startsWith("54")
                || number.startsWith("55")
        ) {
            return CardBrand.MASTERCARD;
        }

        return CardBrand.OTHER;
    }

    private String fingerprint(String number) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                .digest(number.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                "No se pudo calcular la huella de la tarjeta",
                exception
            );
        }
    }

    private CardResponse toResponse(PaymentCard card) {
        return new CardResponse(
            card.getId(),
            card.getAccount().getId(),
            card.getLastFour(),
            card.getBrand(),
            card.getHolderName(),
            card.getExpiration()
        );
    }
}