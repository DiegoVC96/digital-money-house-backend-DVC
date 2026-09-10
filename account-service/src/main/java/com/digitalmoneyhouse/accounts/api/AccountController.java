package com.digitalmoneyhouse.accounts.api;

import com.digitalmoneyhouse.accounts.api.AccountDtos.AccountResponse;
import com.digitalmoneyhouse.accounts.api.AccountDtos.CreateAccountRequest;
import com.digitalmoneyhouse.accounts.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/internal")
    @PreAuthorize("hasRole('SERVICE')")
    public ResponseEntity<AccountResponse> create(
        @Valid @RequestBody CreateAccountRequest request
    ) {
        AccountResponse response = accountService.create(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @GetMapping("/me/{userId}")
    @PreAuthorize("""
        #userId.toString() == authentication.name
        or hasRole('ADMIN')
    """)
    public AccountResponse getByUserId(@PathVariable UUID userId) {
        return accountService.getByUserId(userId);
    }
}