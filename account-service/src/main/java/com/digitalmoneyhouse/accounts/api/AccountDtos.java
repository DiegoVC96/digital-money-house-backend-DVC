package com.digitalmoneyhouse.accounts.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
}