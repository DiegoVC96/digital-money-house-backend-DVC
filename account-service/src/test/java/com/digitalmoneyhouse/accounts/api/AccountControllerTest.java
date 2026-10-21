package com.digitalmoneyhouse.accounts.api;

import com.digitalmoneyhouse.accounts.api.AccountDtos.TransactionResponse;
import com.digitalmoneyhouse.accounts.domain.TransactionType;
import com.digitalmoneyhouse.accounts.service.AccountService;
import com.digitalmoneyhouse.accounts.service.CardService;
import com.digitalmoneyhouse.common.exception.GlobalExceptionHandler;
import com.digitalmoneyhouse.common.exception.InsufficientFundsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AccountService accountService;

    @Mock
    private CardService cardService;

    @InjectMocks
    private AccountController accountController;

    @BeforeEach
    void configureMockMvc() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(accountController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void createsTransferWithAValidRequest() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        TransactionResponse response = new TransactionResponse(
            transactionId,
            accountId,
            TransactionType.TRANSFER_OUT,
            new BigDecimal("100.00"),
            "Transferencia a mar.cielo.brisa",
            Instant.parse("2026-10-21T12:00:00Z")
        );

        when(accountService.transfer(
            accountId,
            "mar.cielo.brisa",
            new BigDecimal("100.00")
        )).thenReturn(response);

        mockMvc.perform(post("/api/accounts/{accountId}/transferences", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"destination\":\"mar.cielo.brisa\",\"amount\":100.00}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(transactionId.toString()))
            .andExpect(jsonPath("$.type").value("TRANSFER_OUT"))
            .andExpect(jsonPath("$.amount").value(100.00));

        verify(accountService).transfer(
            accountId,
            "mar.cielo.brisa",
            new BigDecimal("100.00")
        );
    }

    @Test
    void rejectsAnInvalidTransferAmountBeforeCallingTheService() throws Exception {
        UUID accountId = UUID.randomUUID();

        mockMvc.perform(post("/api/accounts/{accountId}/transferences", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"destination\":\"mar.cielo.brisa\",\"amount\":0}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("amount")));

        verify(accountService, never()).transfer(any(), any(), any());
    }

    @Test
    void mapsInsufficientFundsToGone() throws Exception {
        UUID accountId = UUID.randomUUID();

        when(accountService.transfer(
            eq(accountId),
            eq("mar.cielo.brisa"),
            eq(new BigDecimal("100.00"))
        )).thenThrow(new InsufficientFundsException("Fondos insuficientes"));

        mockMvc.perform(post("/api/accounts/{accountId}/transferences", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"destination\":\"mar.cielo.brisa\",\"amount\":100.00}"))
            .andExpect(status().isGone())
            .andExpect(jsonPath("$.message").value("Fondos insuficientes"));
    }
}
