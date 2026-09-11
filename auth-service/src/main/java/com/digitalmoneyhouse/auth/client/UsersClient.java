package com.digitalmoneyhouse.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;
import java.util.UUID;

@FeignClient(name = "users-service")
public interface UsersClient {

    record CreateUserRequest(
        UUID id,
        String firstName,
        String lastName,
        String phone,
        String dni,
        String email
    ) {
    }

    record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String phone,
        String dni,
        String email,
        Set<String> roles
    ) {
    }

    record AvailabilityResponse(
        boolean emailExists,
        boolean dniExists
    ) {
    }

    @PostMapping("/api/users/internal")
    UserResponse create(@RequestBody CreateUserRequest request);

    @GetMapping("/api/users/internal/availability")
    AvailabilityResponse checkAvailability(
        @RequestParam String email,
        @RequestParam String dni
    );
}