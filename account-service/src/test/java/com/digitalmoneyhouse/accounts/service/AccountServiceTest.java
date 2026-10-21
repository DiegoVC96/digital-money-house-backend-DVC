package com.digitalmoneyhouse.accounts.service;

import com.digitalmoneyhouse.accounts.api.AccountDtos.CreateAccountRequest;
import com.digitalmoneyhouse.accounts.domain.Account;
import com.digitalmoneyhouse.accounts.repository.AccountRepository;
import com.digitalmoneyhouse.common.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.digitalmoneyhouse.accounts.domain.Transaction;
import com.digitalmoneyhouse.accounts.domain.TransactionType;
import com.digitalmoneyhouse.accounts.repository.TransactionRepository;
import com.digitalmoneyhouse.accounts.repository.PaymentCardRepository;
import com.digitalmoneyhouse.accounts.repository.MoneyTransferRepository;
import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import com.digitalmoneyhouse.accounts.api.AccountDtos.UpdateAccountRequest;
import com.digitalmoneyhouse.accounts.domain.ActivityDirection;
import com.digitalmoneyhouse.accounts.domain.ActivityAmountRange;
import com.digitalmoneyhouse.accounts.domain.PaymentCard;
import com.digitalmoneyhouse.accounts.domain.MoneyTransfer;
import com.digitalmoneyhouse.accounts.domain.CardBrand;
import com.digitalmoneyhouse.accounts.api.AccountDtos.CreateDepositRequest;
import com.digitalmoneyhouse.common.exception.ResourceNotFoundException;
import com.digitalmoneyhouse.common.exception.InsufficientFundsException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private MoneyTransferRepository moneyTransferRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createsAccountWithCvuAliasAndZeroBalance() {
        UUID userId = UUID.randomUUID();

        CreateAccountRequest request = new CreateAccountRequest(
            userId,
            "Ana Pérez"
        );

        when(aliasWordsProvider.randomWord(
            ArgumentMatchers.any(SecureRandom.class)
        )).thenReturn("sol", "luna", "rio");

        when(accountRepository.findByUserId(userId))
            .thenReturn(Optional.empty());

        when(accountRepository.save(ArgumentMatchers.any(Account.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var response = accountService.create(request);

        assertEquals(userId, response.userId());
        assertTrue(response.cvu().matches("\\d{22}"));
        assertEquals("sol.luna.rio", response.alias());
        assertEquals(0, response.balance().compareTo(BigDecimal.ZERO));

        verify(accountRepository).save(ArgumentMatchers.any(Account.class));
    }

    @Test
    void rejectsSecondAccountForTheSameUser() {
        UUID userId = UUID.randomUUID();

        CreateAccountRequest request = new CreateAccountRequest(
            userId,
            "Ana Pérez"
        );

        Account existingAccount = new Account(
            userId,
            "1234567890123456789012",
            "ana.dmh.123abc",
            "Ana Pérez"
        );

        when(accountRepository.findByUserId(userId))
            .thenReturn(Optional.of(existingAccount));

        assertThrows(
            ConflictException.class,
            () -> accountService.create(request)
        );

        verify(accountRepository, never())
            .save(ArgumentMatchers.any(Account.class));
    }

    @Mock
    private AliasWordsProvider aliasWordsProvider;

    @AfterEach
void clearSecurityContext() {
    SecurityContextHolder.clearContext();
}

@Test
void returnsRecentTransactionsForTheAccountOwner() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();

    Account account = new Account(
        userId,
        "1234567890123456789012",
        "sol.luna.rio",
        "Ana Pérez"
    );

    Transaction transaction = new Transaction(
        account,
        TransactionType.DEPOSIT,
        new BigDecimal("2500.00"),
        "Ingreso de dinero"
    );

    authenticateAs(userId);

    when(accountRepository.findById(accountId))
        .thenReturn(Optional.of(account));

    when(transactionRepository.findByAccount_IdOrderByCreatedAtDesc(
        eq(accountId),
        any(Pageable.class)
    )).thenReturn(List.of(transaction));

    var response = accountService.getRecentTransactions(accountId, 5);

    assertEquals(1, response.size());
    assertEquals(TransactionType.DEPOSIT, response.getFirst().type());
    assertEquals(
        0,
        response.getFirst().amount()
            .compareTo(new BigDecimal("2500.00"))
    );

    ArgumentCaptor<Pageable> pageableCaptor =
        ArgumentCaptor.forClass(Pageable.class);

    verify(transactionRepository)
        .findByAccount_IdOrderByCreatedAtDesc(
            eq(accountId),
            pageableCaptor.capture()
        );

    assertEquals(5, pageableCaptor.getValue().getPageSize());
}

@Test
void rejectsRecentTransactionsForAnotherUser() {
    UUID accountOwnerId = UUID.randomUUID();
    UUID otherUserId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();

    Account account = new Account(
        accountOwnerId,
        "1234567890123456789012",
        "sol.luna.rio",
        "Ana Pérez"
    );

    authenticateAs(otherUserId);

    when(accountRepository.findById(accountId))
        .thenReturn(Optional.of(account));

    assertThrows(
        AccessDeniedException.class,
        () -> accountService.getRecentTransactions(accountId, 5)
    );

    verifyNoInteractions(transactionRepository);
}

@Test
void updatesAliasForTheAccountOwner() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();

    Account account = new Account(
        userId,
        "1234567890123456789012",
        "sol.luna.rio",
        "Ana Pérez"
    );

    authenticateAs(userId);

    when(accountRepository.findById(accountId))
        .thenReturn(Optional.of(account));

    when(accountRepository.existsByAlias("cielo.mar.brisa"))
        .thenReturn(false);

    var response = accountService.update(
        accountId,
        new UpdateAccountRequest("cielo.mar.brisa")
    );

    assertEquals("cielo.mar.brisa", response.alias());
    assertEquals("1234567890123456789012", response.cvu());
    assertEquals(0, response.balance().compareTo(BigDecimal.ZERO));
}

@Test
void updatesAccountHolderNameForTheUser() {
    UUID userId = UUID.randomUUID();
    Account account = new Account(
        userId,
        "1234567890123456789012",
        "sol.luna.rio",
        "Ana Pérez"
    );

    when(accountRepository.findByUserId(userId))
        .thenReturn(Optional.of(account));

    var response = accountService.updateHolderName(
        userId, "Ana María Pérez"
    );

    assertEquals("Ana María Pérez", response.holderName());
    assertEquals("Ana María Pérez", account.getHolderName());
}

@Test
void returnsActivityOrderedAndFilteredForTheAccountOwner() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    Account account = new Account(
        userId,
        "1234567890123456789012",
        "sol.luna.rio",
        "Ana Pérez"
    );
    Transaction deposit = new Transaction(
        account,
        TransactionType.DEPOSIT,
        new BigDecimal("2500.00"),
        "Ingreso de dinero"
    );
    Transaction expense = new Transaction(
        account,
        TransactionType.TRANSFER_OUT,
        new BigDecimal("500.00"),
        "Transferencia enviada"
    );

    authenticateAs(userId);
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
    when(transactionRepository.findByAccount_IdOrderByCreatedAtDesc(accountId))
        .thenReturn(List.of(deposit, expense));

    var response = accountService.getActivity(
        accountId,
        null,
        null,
        ActivityDirection.INCOME,
        null
    );

    assertEquals(1, response.size());
    assertEquals(TransactionType.DEPOSIT, response.getFirst().type());
}

@Test
void returnsAllActivityWhenNoFiltersAreProvided() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    Account account = new Account(
        userId,
        "1234567890123456789012",
        "sol.luna.rio",
        "Ana Pérez"
    );
    Transaction deposit = new Transaction(
        account,
        TransactionType.DEPOSIT,
        new BigDecimal("2500.00"),
        "Ingreso de dinero"
    );
    Transaction expense = new Transaction(
        account,
        TransactionType.TRANSFER_OUT,
        new BigDecimal("500.00"),
        "Transferencia enviada"
    );

    authenticateAs(userId);
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
    when(transactionRepository.findByAccount_IdOrderByCreatedAtDesc(accountId))
        .thenReturn(List.of(deposit, expense));

    var response = accountService.getActivity(
        accountId, null, null, null, null
    );

    assertEquals(2, response.size());
    assertEquals(TransactionType.DEPOSIT, response.getFirst().type());
}

@Test
void rejectsActivityForAnotherUser() {
    UUID accountOwnerId = UUID.randomUUID();
    UUID otherUserId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    Account account = new Account(
        accountOwnerId,
        "1234567890123456789012",
        "sol.luna.rio",
        "Ana Pérez"
    );

    authenticateAs(otherUserId);
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

    assertThrows(
        AccessDeniedException.class,
        () -> accountService.getActivity(accountId, null, null, null, null)
    );
    verifyNoInteractions(transactionRepository);
}

@Test
void returnsActivityDetailForTheAccountOwner() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    UUID transactionId = UUID.randomUUID();
    Account account = new Account(
        userId,
        "1234567890123456789012",
        "sol.luna.rio",
        "Ana Pérez"
    );
    Transaction transaction = new Transaction(
        account,
        TransactionType.DEPOSIT,
        new BigDecimal("1500.00"),
        "Ingreso de dinero"
    );

    authenticateAs(userId);
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
    when(transactionRepository.findByIdAndAccount_Id(transactionId, accountId))
        .thenReturn(Optional.of(transaction));

    var response = accountService.getActivityDetail(accountId, transactionId);

    assertEquals(TransactionType.DEPOSIT, response.type());
    assertEquals("Ingreso de dinero", response.description());
}

@Test
void returnsNotFoundWhenActivityDoesNotBelongToTheAccount() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    UUID transactionId = UUID.randomUUID();
    Account account = new Account(
        userId,
        "1234567890123456789012",
        "sol.luna.rio",
        "Ana Pérez"
    );

    authenticateAs(userId);
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
    when(transactionRepository.findByIdAndAccount_Id(transactionId, accountId))
        .thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> accountService.getActivityDetail(accountId, transactionId)
    );
}

@Test
void depositsMoneyWithAnAssociatedCard() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    UUID cardId = UUID.randomUUID();
    Account account = new Account(
        userId,
        "1234567890123456789012",
        "sol.luna.rio",
        "Ana Pérez"
    );
    PaymentCard card = new PaymentCard(
        account,
        "fingerprint",
        "1111",
        CardBrand.VISA,
        "ANA PEREZ",
        "1028"
    );

    authenticateAs(userId);
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
    when(paymentCardRepository.findByIdAndAccount_Id(cardId, accountId))
        .thenReturn(Optional.of(card));
    when(transactionRepository.save(any(Transaction.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response = accountService.deposit(
        accountId,
        new CreateDepositRequest(cardId, new BigDecimal("1250.00"))
    );

    assertEquals(TransactionType.DEPOSIT, response.type());
    assertEquals(0, response.amount().compareTo(new BigDecimal("1250.00")));
    assertEquals(0, account.getBalance().compareTo(new BigDecimal("1250.00")));
    assertTrue(response.description().contains("1111"));
}

@Test
void rejectsDepositWithAnExpiredCard() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    UUID cardId = UUID.randomUUID();
    Account account = new Account(
        userId,
        "1234567890123456789012",
        "sol.luna.rio",
        "Ana Pérez"
    );
    PaymentCard card = new PaymentCard(
        account,
        "fingerprint",
        "1111",
        CardBrand.VISA,
        "ANA PEREZ",
        YearMonth.now().minusMonths(1)
            .format(DateTimeFormatter.ofPattern("MMyy"))
    );

    authenticateAs(userId);
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
    when(paymentCardRepository.findByIdAndAccount_Id(cardId, accountId))
        .thenReturn(Optional.of(card));

    assertThrows(
        IllegalArgumentException.class,
        () -> accountService.deposit(
            accountId,
            new CreateDepositRequest(cardId, new BigDecimal("1250.00"))
        )
    );

    verify(transactionRepository, never()).save(any(Transaction.class));
}

@Test
void rejectsDepositWithAnInvalidAmountWithoutCreatingATransaction() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    UUID cardId = UUID.randomUUID();
    Account account = new Account(
        userId,
        "1234567890123456789012",
        "sol.luna.rio",
        "Ana Pérez"
    );
    PaymentCard card = new PaymentCard(
        account,
        "fingerprint",
        "1111",
        CardBrand.VISA,
        "ANA PEREZ",
        YearMonth.now().plusMonths(1)
            .format(DateTimeFormatter.ofPattern("MMyy"))
    );

    authenticateAs(userId);
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
    when(paymentCardRepository.findByIdAndAccount_Id(cardId, accountId))
        .thenReturn(Optional.of(card));

    assertThrows(
        IllegalArgumentException.class,
        () -> accountService.deposit(
            accountId,
            new CreateDepositRequest(cardId, BigDecimal.ZERO)
        )
    );

    assertEquals(0, account.getBalance().compareTo(BigDecimal.ZERO));
    verify(transactionRepository, never()).save(any(Transaction.class));
}

@Test
void rejectsDepositWithCardFromAnotherAccount() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    UUID cardId = UUID.randomUUID();
    Account account = new Account(
        userId,
        "1234567890123456789012",
        "sol.luna.rio",
        "Ana Pérez"
    );

    authenticateAs(userId);
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
    when(paymentCardRepository.findByIdAndAccount_Id(cardId, accountId))
        .thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> accountService.deposit(
            accountId,
            new CreateDepositRequest(cardId, new BigDecimal("1250.00"))
        )
    );

    verify(transactionRepository, never()).save(any(Transaction.class));
}

@Test
void transfersMoneyBetweenAccountsAndRegistersBothMovements() {
    UUID sourceUserId = UUID.randomUUID();
    UUID sourceAccountId = UUID.randomUUID();
    Account source = new Account(
        sourceUserId, "1234567890123456789012", "sol.luna.rio", "Ana Pérez"
    );
    source.credit(new BigDecimal("1000.00"));
    Account destination = new Account(
        UUID.randomUUID(), "9876543210987654321098", "mar.cielo.brisa", "Bruno Díaz"
    );

    authenticateAs(sourceUserId);
    when(accountRepository.findById(sourceAccountId)).thenReturn(Optional.of(source));
    when(accountRepository.findByAlias("mar.cielo.brisa"))
        .thenReturn(Optional.of(destination));
    when(transactionRepository.save(any(Transaction.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response = accountService.transfer(
        sourceAccountId, "mar.cielo.brisa", new BigDecimal("250.00")
    );

    assertEquals(TransactionType.TRANSFER_OUT, response.type());
    assertEquals(0, source.getBalance().compareTo(new BigDecimal("750.00")));
    assertEquals(0, destination.getBalance().compareTo(new BigDecimal("250.00")));
    verify(transactionRepository, times(2)).save(any(Transaction.class));
    verify(moneyTransferRepository).save(any());
}

@Test
void rejectsTransferWhenDestinationDoesNotExist() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    Account source = new Account(
        userId, "1234567890123456789012", "sol.luna.rio", "Ana Pérez"
    );
    source.credit(new BigDecimal("1000.00"));

    authenticateAs(userId);
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(source));
    when(accountRepository.findByAlias("mar.cielo.brisa"))
        .thenReturn(Optional.empty());

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> accountService.transfer(
            accountId, "mar.cielo.brisa", new BigDecimal("100.00")
        )
    );

    assertEquals("La cuenta de destino no existe", exception.getMessage());
    assertEquals(0, source.getBalance().compareTo(new BigDecimal("1000.00")));
}

@Test
void rejectsTransferWhenBalanceIsInsufficient() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    Account source = new Account(
        userId, "1234567890123456789012", "sol.luna.rio", "Ana Pérez"
    );
    source.credit(new BigDecimal("100.00"));
    Account destination = new Account(
        UUID.randomUUID(), "9876543210987654321098", "mar.cielo.brisa", "Bruno Díaz"
    );

    authenticateAs(userId);
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(source));
    when(accountRepository.findByAlias("mar.cielo.brisa"))
        .thenReturn(Optional.of(destination));

    assertThrows(
        InsufficientFundsException.class,
        () -> accountService.transfer(
            accountId, "mar.cielo.brisa", new BigDecimal("101.00")
        )
    );

    assertEquals(0, source.getBalance().compareTo(new BigDecimal("100.00")));
    assertEquals(0, destination.getBalance().compareTo(BigDecimal.ZERO));
    verifyNoInteractions(transactionRepository, moneyTransferRepository);
}

@Test
void rejectsTransferToTheSameAccount() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    Account account = new Account(
        userId, "1234567890123456789012", "sol.luna.rio", "Ana Pérez"
    );
    account.credit(new BigDecimal("100.00"));

    authenticateAs(userId);
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
    when(accountRepository.findByAlias("sol.luna.rio"))
        .thenReturn(Optional.of(account));

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> accountService.transfer(
            accountId, "sol.luna.rio", new BigDecimal("10.00")
        )
    );

    assertEquals(
        "No podés transferir dinero a tu propia cuenta",
        exception.getMessage()
    );
    assertEquals(0, account.getBalance().compareTo(new BigDecimal("100.00")));
    verifyNoInteractions(transactionRepository, moneyTransferRepository);
}

@Test
void rejectsTransferWithAnInvalidAmount() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    Account source = new Account(
        userId, "1234567890123456789012", "sol.luna.rio", "Ana Pérez"
    );
    source.credit(new BigDecimal("100.00"));
    Account destination = new Account(
        UUID.randomUUID(), "9876543210987654321098", "mar.cielo.brisa", "Bruno Díaz"
    );

    authenticateAs(userId);
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(source));
    when(accountRepository.findByAlias("mar.cielo.brisa"))
        .thenReturn(Optional.of(destination));

    assertThrows(
        IllegalArgumentException.class,
        () -> accountService.transfer(
            accountId, "mar.cielo.brisa", BigDecimal.ZERO
        )
    );

    assertEquals(0, source.getBalance().compareTo(new BigDecimal("100.00")));
    assertEquals(0, destination.getBalance().compareTo(BigDecimal.ZERO));
    verifyNoInteractions(transactionRepository, moneyTransferRepository);
}

@Test
void rejectsTransferFromAnAccountOwnedByAnotherUser() {
    UUID ownerId = UUID.randomUUID();
    UUID otherUserId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    Account source = new Account(
        ownerId, "1234567890123456789012", "sol.luna.rio", "Ana Pérez"
    );

    authenticateAs(otherUserId);
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(source));

    assertThrows(
        AccessDeniedException.class,
        () -> accountService.transfer(
            accountId, "mar.cielo.brisa", new BigDecimal("10.00")
        )
    );

    verify(accountRepository, never()).findByAlias(any());
    verifyNoInteractions(transactionRepository, moneyTransferRepository);
}

@Test
void allowsOnlyOneConcurrentDebitWhenBothOperationsUseTheFullBalance()
    throws Exception {

    Account account = new Account(
        UUID.randomUUID(),
        "1234567890123456789012",
        "sol.luna.rio",
        "Ana Pérez"
    );
    account.credit(new BigDecimal("100.00"));

    CountDownLatch ready = new CountDownLatch(2);
    CountDownLatch start = new CountDownLatch(1);
    ExecutorService executor = Executors.newFixedThreadPool(2);

    try {
        Future<Boolean> first = executor.submit(() -> debitWhenReady(
            account, new BigDecimal("100.00"), ready, start
        ));
        Future<Boolean> second = executor.submit(() -> debitWhenReady(
            account, new BigDecimal("100.00"), ready, start
        ));

        ready.await();
        start.countDown();

        int successfulDebits = (first.get() ? 1 : 0) + (second.get() ? 1 : 0);

        assertEquals(1, successfulDebits);
        assertEquals(0, account.getBalance().compareTo(BigDecimal.ZERO));
    } finally {
        executor.shutdownNow();
    }
}

@Test
void returnsRecentRecipientsForTheAccountOwner() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    Account source = new Account(
        userId, "1234567890123456789012", "sol.luna.rio", "Ana Pérez"
    );
    Account destination = new Account(
        UUID.randomUUID(), "9876543210987654321098", "mar.cielo.brisa", "Bruno Díaz"
    );

    authenticateAs(userId);
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(source));
    when(moneyTransferRepository.findBySourceAccount_IdOrderByCreatedAtDesc(accountId))
        .thenReturn(List.of(new MoneyTransfer(
            source, destination, new BigDecimal("100.00")
        )));

    var recipients = accountService.getRecentRecipients(accountId);

    assertEquals(1, recipients.size());
    assertEquals("mar.cielo.brisa", recipients.getFirst().alias());
    assertEquals("Bruno Díaz", recipients.getFirst().holderName());
}

@Test
void rejectsRecipientsFromAnotherAccount() {
    UUID ownerId = UUID.randomUUID();
    UUID otherUserId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    Account account = new Account(
        ownerId, "1234567890123456789012", "sol.luna.rio", "Ana Pérez"
    );

    authenticateAs(otherUserId);
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

    assertThrows(
        AccessDeniedException.class,
        () -> accountService.getRecentRecipients(accountId)
    );

    verifyNoInteractions(moneyTransferRepository);
}

@Test
void filtersActivityByAmountRange() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    Account account = new Account(
        userId,
        "1234567890123456789012",
        "sol.luna.rio",
        "Ana Pérez"
    );
    Transaction lowerDeposit = new Transaction(
        account,
        TransactionType.DEPOSIT,
        new BigDecimal("500.00"),
        "Ingreso menor"
    );
    Transaction higherDeposit = new Transaction(
        account,
        TransactionType.DEPOSIT,
        new BigDecimal("1500.00"),
        "Ingreso mayor"
    );

    authenticateAs(userId);
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
    when(transactionRepository.findByAccount_IdOrderByCreatedAtDesc(accountId))
        .thenReturn(List.of(higherDeposit, lowerDeposit));

    var response = accountService.getActivity(
        accountId,
        null,
        null,
        null,
        ActivityAmountRange.ZERO_TO_1000
    );

    assertEquals(1, response.size());
    assertEquals(0, response.getFirst().amount()
        .compareTo(new BigDecimal("500.00")));
}

@Test
void rejectsActivitySearchWithAnInvalidPeriod() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    Account account = new Account(
        userId,
        "1234567890123456789012",
        "sol.luna.rio",
        "Ana Pérez"
    );

    authenticateAs(userId);
    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

    assertThrows(
        IllegalArgumentException.class,
        () -> accountService.getActivity(
            accountId,
            Instant.parse("2026-10-08T00:00:00Z"),
            Instant.parse("2026-10-07T00:00:00Z"),
            null,
            null
        )
    );

    verifyNoInteractions(transactionRepository);
}

private void authenticateAs(UUID userId) {
    var authentication = new UsernamePasswordAuthenticationToken(
        userId.toString(),
        null,
        List.of(new SimpleGrantedAuthority("ROLE_USER"))
    );

    SecurityContextHolder.getContext().setAuthentication(authentication);
}

private boolean debitWhenReady(
    Account account,
    BigDecimal amount,
    CountDownLatch ready,
    CountDownLatch start
) throws InterruptedException {
    ready.countDown();
    start.await();

    try {
        account.debit(amount);
        return true;
    } catch (InsufficientFundsException exception) {
        return false;
    }
}
}
