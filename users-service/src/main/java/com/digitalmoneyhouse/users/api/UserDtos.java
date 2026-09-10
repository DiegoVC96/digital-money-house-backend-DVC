package com.digitalmoneyhouse.users.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public final class UserDtos {

    private UserDtos() {
    }

    public record CreateUserRequest(
        @NotNull UUID id,

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
        String email
    ) {
    }

    public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String phone,
        String dni,
        String email,
        Set<String> roles
    ) {
    }
}