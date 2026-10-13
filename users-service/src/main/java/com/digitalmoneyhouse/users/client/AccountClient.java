package com.digitalmoneyhouse.users.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(name = "account-service")
public interface AccountClient {

    record UpdateHolderNameRequest(String holderName) {
    }

    @PutMapping("/api/accounts/internal/users/{userId}/holder-name")
    void updateHolderName(
        @PathVariable UUID userId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
        @RequestBody UpdateHolderNameRequest request
    );
}
