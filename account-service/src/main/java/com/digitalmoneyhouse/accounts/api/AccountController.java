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
import com.digitalmoneyhouse.accounts.api.AccountDtos.TransactionResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestParam;
import com.digitalmoneyhouse.accounts.api.AccountDtos.UpdateAccountRequest;
import org.springframework.web.bind.annotation.PatchMapping;
import com.digitalmoneyhouse.accounts.api.AccountDtos.CardResponse;
import com.digitalmoneyhouse.accounts.api.AccountDtos.CreateCardRequest;
import com.digitalmoneyhouse.accounts.service.CardService;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.UUID;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final CardService cardService;

    public AccountController(
        AccountService accountService,
        CardService cardService
    ) {
        this.accountService = accountService;
        this.cardService = cardService;
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
    public AccountResponse getByUserId(
    @PathVariable("userId") UUID userId
    ) {
        return accountService.getByUserId(userId);
    }

    @GetMapping("/{accountId}")
    @PreAuthorize("isAuthenticated()")
    public AccountResponse getById(
        @PathVariable("accountId") UUID accountId
    ) {
        return accountService.getById(accountId);
    }

    @PatchMapping("/{accountId}")
    @PreAuthorize("isAuthenticated()")
    public AccountResponse update(
        @PathVariable("accountId") UUID accountId,
        @Valid @RequestBody UpdateAccountRequest request
    ) {
        return accountService.update(accountId, request);
    }

    @GetMapping("/{accountId}/transactions")
    @PreAuthorize("isAuthenticated()")
    public List<TransactionResponse> getRecentTransactions(
        @PathVariable("accountId") UUID accountId,
        @RequestParam(defaultValue = "5")
        @Min(1) @Max(5) int limit
    ) {
        return accountService.getRecentTransactions(accountId, limit);
    }

    @PostMapping("/{accountId}/cards")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CardResponse> createCard(
        @PathVariable UUID accountId,
        @Valid @RequestBody CreateCardRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(cardService.create(accountId, request));
    }

    @GetMapping("/{accountId}/cards")
    @PreAuthorize("isAuthenticated()")
    public List<CardResponse> getCards(
        @PathVariable UUID accountId
    ) {
        return cardService.findAllByAccount(accountId);
    }

    @GetMapping("/{accountId}/cards/{cardId}")
    @PreAuthorize("isAuthenticated()")
    public CardResponse getCard(
        @PathVariable UUID accountId,
        @PathVariable UUID cardId
    ) {
        return cardService.findById(accountId, cardId);
    }

    @DeleteMapping("/{accountId}/cards/{cardId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteCard(
        @PathVariable UUID accountId,
        @PathVariable UUID cardId
    ) {
        cardService.delete(accountId, cardId);

        return ResponseEntity.ok().build();
    }
}