package com.digitalmoneyhouse.accounts.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.digitalmoneyhouse.accounts.domain.TransactionType;
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
}