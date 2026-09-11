package com.digitalmoneyhouse.auth.service;

import com.digitalmoneyhouse.auth.api.AuthDtos.RegisterRequest;
import com.digitalmoneyhouse.auth.client.AccountsClient;
import com.digitalmoneyhouse.auth.client.UsersClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.digitalmoneyhouse.auth.domain.VerificationPurpose;
import com.digitalmoneyhouse.auth.api.AuthDtos.PasswordRecoveryConfirmRequest;
import com.digitalmoneyhouse.auth.api.AuthDtos.PasswordRecoveryRequest;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private KeycloakIdentityService keycloakIdentityService;

    @Mock
    private KeycloakTokenService keycloakTokenService;

    @Mock
    private UsersClient usersClient;

    @Mock
    private AccountsClient accountsClient;

    @Mock
    private VerificationCodeService verificationCodeService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registersUserCreatesAccountAndReturnsTokens() {
        UUID userId = UUID.randomUUID();

        RegisterRequest request = new RegisterRequest(
            "Lucia",
            "Gomez",
            "1198765432",
            "34567890",
            "lucia@example.com",
            "ClaveSegura123"
        );

        when(usersClient.checkAvailability(
            "lucia@example.com",
            "34567890"
        )).thenReturn(
            new UsersClient.AvailabilityResponse(false, false)
        );

        when(keycloakIdentityService.createUser(
            "lucia@example.com",
            "ClaveSegura123",
            "Lucia",
            "Gomez"
        )).thenReturn(userId);

        when(usersClient.create(any())).thenReturn(
            new UsersClient.UserResponse(
                userId,
                "Lucia",
                "Gomez",
                "1198765432",
                "34567890",
                "lucia@example.com",
                Set.of("USER")
            )
        );

        when(accountsClient.create(any())).thenReturn(
            new AccountsClient.AccountResponse(
                UUID.randomUUID(),
                userId,
                "1234567890123456789012",
                "sol.luna.rio",
                BigDecimal.ZERO,
                "Lucia Gomez"
            )
        );

        when(keycloakTokenService.login(
            "lucia@example.com",
            "ClaveSegura123"
        )).thenReturn(
            new KeycloakTokenService.UserToken(
                "access-token",
                "refresh-token",
                "Bearer"
            )
        );

        var response = authService.register(request);

        assertEquals(userId, response.userId());
        assertEquals("lucia@example.com", response.user().email());
        assertEquals("sol.luna.rio", response.account().alias());
        assertEquals("1234567890123456789012", response.account().cvu());
        assertEquals("1198765432", response.user().phone());
        assertEquals("34567890", response.user().dni());
        assertNotNull(response.accessToken());

        verify(usersClient).create(any());
        verify(accountsClient).create(any());
        verify(verificationCodeService).issueCode(
            "lucia@example.com",
            VerificationPurpose.EMAIL_VERIFICATION
        );
    }

    @Test
void sendsPasswordResetCodeWhenEmailExists() {
    when(usersClient.checkAvailability(
        "lucia@example.com",
        "00000000"
    )).thenReturn(
        new UsersClient.AvailabilityResponse(true, false)
    );

    authService.requestPasswordRecovery(
        new PasswordRecoveryRequest("lucia@example.com")
    );

    verify(verificationCodeService).issueCode(
        "lucia@example.com",
        VerificationPurpose.PASSWORD_RESET
    );
}

@Test
void verifiesCodeAndResetsPassword() {
    PasswordRecoveryConfirmRequest request =
        new PasswordRecoveryConfirmRequest(
            "lucia@example.com",
            "123456",
            "NuevaClaveSegura123"
        );

    authService.confirmPasswordRecovery(request);

    verify(verificationCodeService).verifyCode(
        "lucia@example.com",
        "123456",
        VerificationPurpose.PASSWORD_RESET
    );

    verify(keycloakIdentityService).resetPassword(
        "lucia@example.com",
        "NuevaClaveSegura123"
    );
}

    @Test
    void delegatesLogoutToKeycloakForAuthenticatedUser() {
        UUID userId = UUID.randomUUID();

        authService.logout(userId);

        verify(keycloakIdentityService).logoutUser(userId);
    }
}