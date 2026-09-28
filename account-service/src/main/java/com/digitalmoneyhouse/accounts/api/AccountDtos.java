package com.digitalmoneyhouse.accounts.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import com.digitalmoneyhouse.accounts.domain.TransactionType;
import com.digitalmoneyhouse.accounts.domain.CardBrand;
import java.time.Instant;

import java.math.BigDecimal;
import java.util.UUID;

public final class AccountDtos {

    private AccountDtos() {
    }

    public record CreateAccountRequest(
        @NotNull UUID userId,

        @NotBlank
        String holderName
    ) {
    }

    public record AccountResponse(
        UUID id,
        UUID userId,
        String cvu,
        String alias,
        BigDecimal balance,
        String holderName
    ) {
    }

    public record TransactionResponse(
        UUID id,
        UUID accountId,
        TransactionType type,
        BigDecimal amount,
        String description,
        Instant createdAt
    ) {
    }

    public record UpdateAccountRequest(
        @NotBlank
        @Pattern(
            regexp = "[a-z]+\\.[a-z]+\\.[a-z]+",
            message = "El alias debe tener tres palabras en minúscula"
        )
        @Size(max = 80)
        String alias
    ) {
    }

    public record CreateCardRequest(
        @NotBlank
        @Pattern(regexp = "\\d{13,19}")
        String number,

        @NotBlank
        @Size(min = 2, max = 100)
        String name,

        @NotBlank
        @Pattern(regexp = "\\d{4}")
        String expiration,

        @NotBlank
        @Pattern(regexp = "\\d{3,4}")
        String cvc
    ) {
    }

    public record CardResponse(
        UUID id,
        UUID accountId,
        String lastFour,
        CardBrand brand,
        String holderName,
        String expiration
    ) {
    }
}