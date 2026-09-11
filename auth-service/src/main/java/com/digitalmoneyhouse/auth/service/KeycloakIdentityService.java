package com.digitalmoneyhouse.auth.service;

import com.digitalmoneyhouse.common.exception.ConflictException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class KeycloakIdentityService {

    private final RestClient restClient;
    private final KeycloakTokenService tokenService;
    private final String baseUrl;
    private final String realm;

    public KeycloakIdentityService(
        RestClient.Builder restClientBuilder,
        KeycloakTokenService tokenService,
        @Value("${keycloak.base-url}") String baseUrl,
        @Value("${keycloak.realm}") String realm
    ) {
        this.restClient = restClientBuilder.build();
        this.tokenService = tokenService;
        this.baseUrl = baseUrl;
        this.realm = realm;
    }

    public UUID createUser(
        String email,
        String password,
        String firstName,
        String lastName
    ) {
        String token = tokenService.getServiceAccessToken();

        Map<String, Object> user = new HashMap<>();
        user.put("username", email);
        user.put("email", email);
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("enabled", true);
        user.put("emailVerified", true);
        user.put(
            "credentials",
            List.of(
                Map.of(
                    "type", "password",
                    "value", password,
                    "temporary", false
                )
            )
        );

        String location = restClient.post()
            .uri(baseUrl + "/admin/realms/" + realm + "/users")
            .headers(headers -> headers.setBearerAuth(token))
            .contentType(MediaType.APPLICATION_JSON)
            .body(user)
            .exchange((request, response) -> {
                if (response.getStatusCode().value() == 409) {
                    throw new ConflictException(
                        "Ya existe un usuario con ese email"
                    );
                }

                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new IllegalStateException(
                        "No se pudo crear el usuario en Keycloak"
                    );
                }

                return response.getHeaders()
                    .getFirst(HttpHeaders.LOCATION);
            });

        if (location == null) {
            throw new IllegalStateException(
                "Keycloak no devolvió la ubicación del usuario creado"
            );
        }

        UUID userId = UUID.fromString(
            location.substring(location.lastIndexOf("/") + 1)
        );

        assignUserRole(userId, token);

        return userId;
    }

    private void assignUserRole(UUID userId, String token) {
        Map<?, ?> userRole = restClient.get()
            .uri(baseUrl + "/admin/realms/" + realm + "/roles/USER")
            .headers(headers -> headers.setBearerAuth(token))
            .retrieve()
            .body(Map.class);

        if (userRole == null) {
            throw new IllegalStateException(
                "No se encontró el rol USER en Keycloak"
            );
        }

        restClient.post()
            .uri(baseUrl + "/admin/realms/" + realm
                + "/users/" + userId
                + "/role-mappings/realm")
            .headers(headers -> headers.setBearerAuth(token))
            .contentType(MediaType.APPLICATION_JSON)
            .body(List.of(userRole))
            .retrieve()
            .toBodilessEntity();
    }
}