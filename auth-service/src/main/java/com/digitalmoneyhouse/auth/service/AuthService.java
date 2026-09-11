package com.digitalmoneyhouse.auth.service;

import com.digitalmoneyhouse.auth.api.AuthDtos.AccountSummary;
import com.digitalmoneyhouse.auth.api.AuthDtos.AuthResponse;
import com.digitalmoneyhouse.auth.api.AuthDtos.LoginRequest;
import com.digitalmoneyhouse.auth.api.AuthDtos.RegisterRequest;
import com.digitalmoneyhouse.auth.api.AuthDtos.RegisteredUser;
import com.digitalmoneyhouse.auth.client.AccountsClient;
import com.digitalmoneyhouse.auth.client.UsersClient;
import org.springframework.stereotype.Service;
import com.digitalmoneyhouse.common.exception.ConflictException;
import com.digitalmoneyhouse.common.exception.ResourceNotFoundException;

import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    private final KeycloakIdentityService keycloakIdentityService;
    private final KeycloakTokenService keycloakTokenService;
    private final UsersClient usersClient;
    private final AccountsClient accountsClient;

    public AuthService(
        KeycloakIdentityService keycloakIdentityService,
        KeycloakTokenService keycloakTokenService,
        UsersClient usersClient,
        AccountsClient accountsClient
    ) {
        this.keycloakIdentityService = keycloakIdentityService;
        this.keycloakTokenService = keycloakTokenService;
        this.usersClient = usersClient;
        this.accountsClient = accountsClient;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = request.email().toLowerCase(Locale.ROOT);
        UsersClient.AvailabilityResponse availability =
            usersClient.checkAvailability(email, request.dni());

        if (availability.emailExists() || availability.dniExists()) {
            throw new ConflictException(
                "Ya existe un usuario con ese email o DNI"
            );
        }

        UUID userId = keycloakIdentityService.createUser(
            email,
            request.password(),
            request.firstName(),
            request.lastName()
        );

        UsersClient.UserResponse createdUser = usersClient.create(
            new UsersClient.CreateUserRequest(
                userId,
                request.firstName(),
                request.lastName(),
                request.phone(),
                request.dni(),
                email
            )
        );

        AccountsClient.AccountResponse createdAccount = accountsClient.create(
            new AccountsClient.CreateAccountRequest(
                userId,
                request.firstName() + " " + request.lastName()
            )
        );

        KeycloakTokenService.UserToken token =
            keycloakTokenService.login(email, request.password());

        return new AuthResponse(
            token.accessToken(),
            token.refreshToken(),
            token.tokenType(),
            createdUser.id(),
            new RegisteredUser(
                createdUser.id(),
                createdUser.firstName(),
                createdUser.lastName(),
                createdUser.phone(),
                createdUser.dni(),
                createdUser.email(),
                createdUser.roles()
            ),
            new AccountSummary(
                createdAccount.cvu(),
                createdAccount.alias()
            )
        );
    }

    public void logout(UUID userId) {
        keycloakIdentityService.logoutUser(userId);
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().toLowerCase(Locale.ROOT);

        UsersClient.AvailabilityResponse availability =
            usersClient.checkAvailability(email, "00000000");

        if (!availability.emailExists()) {
            throw new ResourceNotFoundException("Usuario inexistente");
        }

        KeycloakTokenService.UserToken token =
            keycloakTokenService.login(email, request.password());

        return toResponse(token, null);
    }

    private AuthResponse toResponse(
        KeycloakTokenService.UserToken token,
        UUID userId
    ) {
        return new AuthResponse(
            token.accessToken(),
            token.refreshToken(),
            token.tokenType(),
            userId,
            null,
            null
        );
    }
}