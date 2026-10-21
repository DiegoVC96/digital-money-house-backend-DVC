package com.digitalmoneyhouse.common.security;

import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;

public class KeycloakServiceTokenProvider {

    private static final long EXPIRATION_SAFETY_SECONDS = 30;

    private final RestClient restClient;
    private final String baseUrl;
    private final String realm;
    private final String clientId;
    private final String clientSecret;
    private volatile CachedToken cachedToken;

    public KeycloakServiceTokenProvider(
        RestClient.Builder restClientBuilder,
        String baseUrl,
        String realm,
        String clientId,
        String clientSecret
    ) {
        this.restClient = restClientBuilder.build();
        this.baseUrl = baseUrl;
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getAccessToken() {
        CachedToken current = cachedToken;
        if (current != null && current.isUsable()) {
            return current.value();
        }

        synchronized (this) {
            current = cachedToken;
            if (current == null || !current.isUsable()) {
                cachedToken = requestToken();
            }

            return cachedToken.value();
        }
    }

    private CachedToken requestToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        Map<?, ?> response = restClient.post()
            .uri(baseUrl + "/realms/" + realm + "/protocol/openid-connect/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(Map.class);

        Object token = response == null ? null : response.get("access_token");
        if (token == null) {
            throw new IllegalStateException(
                "Keycloak no devolvió un token técnico"
            );
        }

        long expiresIn = numberValue(
            response.get("expires_in"),
            60
        );
        long usableSeconds = Math.max(
            1,
            expiresIn - EXPIRATION_SAFETY_SECONDS
        );

        return new CachedToken(
            token.toString(),
            Instant.now().plusSeconds(usableSeconds)
        );
    }

    private long numberValue(Object value, long defaultValue) {
        if (value instanceof Number number) {
            return number.longValue();
        }

        try {
            return value == null ? defaultValue : Long.parseLong(value.toString());
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    private record CachedToken(String value, Instant expiresAt) {
        boolean isUsable() {
            return Instant.now().isBefore(expiresAt);
        }
    }
}
