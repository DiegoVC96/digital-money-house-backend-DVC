package com.digitalmoneyhouse.users.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.UUID;

@FeignClient(name = "auth-service")
public interface IdentityClient {

    record UpdateIdentityRequest(
        String firstName,
        String lastName,
        String email,
        String password
    ) {
    }

    @PutMapping("/api/auth/internal/users/{userId}")
    void updateIdentity(
        @PathVariable UUID userId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
        @RequestBody UpdateIdentityRequest request
    );
}