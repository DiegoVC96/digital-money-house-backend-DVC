package com.digitalmoneyhouse.auth.config;

import com.digitalmoneyhouse.auth.service.KeycloakTokenService;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class FeignSecurityConfig {

    @Bean
    RequestInterceptor serviceTokenInterceptor(
        KeycloakTokenService keycloakTokenService
    ) {
        return requestTemplate -> {
            String token = keycloakTokenService.getServiceAccessToken();

            requestTemplate.header(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + token
            );
        };
    }
}