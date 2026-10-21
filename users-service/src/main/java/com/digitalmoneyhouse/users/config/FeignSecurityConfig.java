package com.digitalmoneyhouse.users.config;

import com.digitalmoneyhouse.common.security.KeycloakServiceTokenProvider;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class FeignSecurityConfig {

    @Bean
    KeycloakServiceTokenProvider keycloakServiceTokenProvider(
        org.springframework.web.client.RestClient.Builder restClientBuilder,
        @Value("${keycloak.base-url}") String baseUrl,
        @Value("${keycloak.realm}") String realm,
        @Value("${keycloak.service-client-id}") String clientId,
        @Value("${keycloak.service-client-secret}") String clientSecret
    ) {
        return new KeycloakServiceTokenProvider(
            restClientBuilder,
            baseUrl,
            realm,
            clientId,
            clientSecret
        );
    }

    @Bean
    RequestInterceptor serviceTokenInterceptor(
        KeycloakServiceTokenProvider serviceTokenProvider
    ) {
        return requestTemplate -> requestTemplate.header(
            HttpHeaders.AUTHORIZATION,
            "Bearer " + serviceTokenProvider.getAccessToken()
        );
    }
}
