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
import com.digitalmoneyhouse.common.security.KeycloakServiceTokenProvider;

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
    private final String publicClientId;
    private final KeycloakServiceTokenProvider serviceTokenProvider;

    public KeycloakTokenService(
        RestClient.Builder restClientBuilder,
        @Value("${keycloak.base-url}") String baseUrl,
        @Value("${keycloak.realm}") String realm,
        @Value("${keycloak.public-client-id}") String publicClientId,
        KeycloakServiceTokenProvider serviceTokenProvider
    ) {
        this.restClient = restClientBuilder.build();
        this.baseUrl = baseUrl;
        this.realm = realm;
        this.publicClientId = publicClientId;
        this.serviceTokenProvider = serviceTokenProvider;
    }

    public String getServiceAccessToken() {
        return serviceTokenProvider.getAccessToken();
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
            throw new InvalidCredentialsException("Credenciales inválidas");
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
