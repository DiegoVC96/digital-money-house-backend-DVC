package com.digitalmoneyhouse.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
        @NotBlank
        @Size(min = 2, max = 80)
        String firstName,

        @NotBlank
        @Size(min = 2, max = 80)
        String lastName,

        @NotBlank
        @Pattern(regexp = "\\d{8,15}")
        String phone,

        @NotBlank
        @Pattern(regexp = "\\d{7,8}")
        String dni,

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 8, max = 72)
        String password
    ) {
    }

    public record LoginRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        String password
    ) {
    }

    public record PasswordRecoveryRequest(
    @NotBlank
    @Email
    String email
) {
}

public record PasswordRecoveryConfirmRequest(
    @NotBlank
    @Email
    String email,

    @NotBlank
    @Pattern(regexp = "\\d{6}")
    String code,

    @NotBlank
    @Size(min = 8, max = 72)
    String newPassword
) {
}

public record EmailVerificationConfirmRequest(
    @NotBlank
    @Pattern(regexp = "\\d{6}")
    String code
) {
}

    public record RegisteredUser(
        UUID id,
        String firstName,
        String lastName,
        String phone,
        String dni,
        String email,
        Set<String> roles
    ) {
    }

    public record AccountSummary(
        String cvu,
        String alias
    ) {
    }

    public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        UUID userId,
        RegisteredUser user,
        AccountSummary account
    ) {
    }
}