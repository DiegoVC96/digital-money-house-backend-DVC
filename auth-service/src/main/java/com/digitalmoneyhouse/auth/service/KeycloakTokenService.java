package com.digitalmoneyhouse.auth.service;

import com.digitalmoneyhouse.common.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import com.digitalmoneyhouse.common.exception.InvalidCredentialsException;

import java.util.Map;

@Service
public class KeycloakTokenService {

    public record UserToken(
        String accessToken,
        String refreshToken,
        String tokenType
    ) {
    }

    private final RestClient restClient;
    private final String baseUrl;
    private final String realm;
    private final String serviceClientId;
    private final String serviceClientSecret;
    private final String publicClientId;

    public KeycloakTokenService(
        RestClient.Builder restClientBuilder,
        @Value("${keycloak.base-url}") String baseUrl,
        @Value("${keycloak.realm}") String realm,
        @Value("${keycloak.service-client-id}") String serviceClientId,
        @Value("${keycloak.service-client-secret}") String serviceClientSecret,
        @Value("${keycloak.public-client-id}") String publicClientId
    ) {
        this.restClient = restClientBuilder.build();
        this.baseUrl = baseUrl;
        this.realm = realm;
        this.serviceClientId = serviceClientId;
        this.serviceClientSecret = serviceClientSecret;
        this.publicClientId = publicClientId;
    }

    public String getServiceAccessToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();

        form.add("grant_type", "client_credentials");
        form.add("client_id", serviceClientId);
        form.add("client_secret", serviceClientSecret);

        Map<?, ?> response = requestToken(form);

        Object token = response.get("access_token");

        if (token == null) {
            throw new IllegalStateException(
                "Keycloak no devolvió un token técnico"
            );
        }

        return token.toString();
    }

    public UserToken login(String email, String password) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();

        form.add("grant_type", "password");
        form.add("client_id", publicClientId);
        form.add("username", email);
        form.add("password", password);

        try {
            Map<?, ?> response = requestToken(form);

            Object accessToken = response.get("access_token");
            Object refreshToken = response.get("refresh_token");
            Object tokenType = response.get("token_type");

            if (accessToken == null) {
                throw new IllegalStateException(
                    "Keycloak no devolvió un token de acceso"
                );
            }

            return new UserToken(
                accessToken.toString(),
                refreshToken == null ? null : refreshToken.toString(),
                tokenType == null ? "Bearer" : tokenType.toString()
            );
        } catch (RestClientResponseException exception) {
            throw new InvalidCredentialsException("Contraseña incorrecta");
        }
    }

    public void logout(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();

        form.add("client_id", publicClientId);
        form.add("refresh_token", refreshToken);

        try {
            restClient.post()
                .uri(baseUrl + "/realms/" + realm
                    + "/protocol/openid-connect/logout")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            throw new UnauthorizedException(
                "No fue posible cerrar la sesión"
            );
        }
    }

    private Map<?, ?> requestToken(
        MultiValueMap<String, String> form
    ) {
        return restClient.post()
            .uri(baseUrl + "/realms/" + realm
                + "/protocol/openid-connect/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(Map.class);
    }
}