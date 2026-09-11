package com.digitalmoneyhouse.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "account-service")
public interface AccountsClient {

    record CreateAccountRequest(
        UUID userId,
        String holderName
    ) {
    }

    record AccountResponse(
        UUID id,
        UUID userId,
        String cvu,
        String alias,
        BigDecimal balance,
        String holderName
    ) {
    }

    @PostMapping("/api/accounts/internal")
    AccountResponse create(@RequestBody CreateAccountRequest request);
}