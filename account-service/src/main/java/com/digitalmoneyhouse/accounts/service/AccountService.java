package com.digitalmoneyhouse.accounts.service;

import com.digitalmoneyhouse.accounts.api.AccountDtos.AccountResponse;
import com.digitalmoneyhouse.accounts.api.AccountDtos.CreateAccountRequest;
import com.digitalmoneyhouse.accounts.domain.Account;
import com.digitalmoneyhouse.accounts.repository.AccountRepository;
import com.digitalmoneyhouse.common.exception.ConflictException;
import com.digitalmoneyhouse.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.digitalmoneyhouse.accounts.api.AccountDtos.TransactionResponse;
import com.digitalmoneyhouse.accounts.domain.Transaction;
import com.digitalmoneyhouse.accounts.repository.TransactionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.digitalmoneyhouse.accounts.api.AccountDtos.UpdateAccountRequest;
import com.digitalmoneyhouse.accounts.domain.ActivityAmountRange;
import com.digitalmoneyhouse.accounts.domain.ActivityDirection;
import com.digitalmoneyhouse.accounts.api.AccountDtos.CreateDepositRequest;
import com.digitalmoneyhouse.accounts.domain.PaymentCard;
import com.digitalmoneyhouse.accounts.domain.TransactionType;
import com.digitalmoneyhouse.accounts.repository.PaymentCardRepository;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;
import java.util.List;

@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final AliasWordsProvider aliasWordsProvider;
    private final SecureRandom secureRandom = new SecureRandom();
    private final TransactionRepository transactionRepository;
    private final PaymentCardRepository paymentCardRepository;

    public AccountService(
        AccountRepository accountRepository,
        TransactionRepository transactionRepository,
        PaymentCardRepository paymentCardRepository,
        AliasWordsProvider aliasWordsProvider
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.paymentCardRepository = paymentCardRepository;
        this.aliasWordsProvider = aliasWordsProvider;
    }

    public AccountResponse create(CreateAccountRequest request) {
        if (accountRepository.findByUserId(request.userId()).isPresent()) {
            throw new ConflictException(
                "El usuario ya posee una cuenta digital"
            );
        }

        Account account = new Account(
            request.userId(),
            generateUniqueCvu(),
            generateUniqueAlias(),
            request.holderName()
        );

        return toResponse(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public AccountResponse getByUserId(UUID userId) {
        Account account = accountRepository.findByUserId(userId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Cuenta no encontrada")
            );

        return toResponse(account);
    }

    @Transactional(readOnly = true)
    public AccountResponse getById(UUID accountId) {
        Account account = findById(accountId);
        validateAccountAccess(account);

        return toResponse(account);
    }

    public AccountResponse update(
        UUID accountId,
        UpdateAccountRequest request
    ) {
        Account account = findById(accountId);
        validateAccountAccess(account);

        boolean aliasChanged = !account.getAlias()
        .equals(request.alias());

        if (aliasChanged && accountRepository.existsByAlias(request.alias())) {
            throw new ConflictException("El alias ya está en uso");
        }

        account.updateAlias(request.alias());

        return toResponse(account);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getRecentTransactions(
        UUID accountId,
        int limit
    ) {
        Account account = findById(accountId);
        validateAccountAccess(account);

        return transactionRepository
            .findByAccount_IdOrderByCreatedAtDesc(
                accountId,
                PageRequest.of(
                    0,
                    limit,
                    Sort.by(Sort.Direction.DESC, "createdAt")
                )
            )
            .stream()
            .map(this::toTransactionResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getActivity(
        UUID accountId,
        Instant from,
        Instant to,
        ActivityDirection type,
        ActivityAmountRange range
    ) {
        Account account = findById(accountId);
        validateAccountAccess(account);

        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException(
                "La fecha inicial no puede ser posterior a la fecha final"
            );
        }

        return transactionRepository
            .findByAccount_IdOrderByCreatedAtDesc(accountId)
            .stream()
            .filter(transaction -> from == null
                || !transaction.getCreatedAt().isBefore(from))
            .filter(transaction -> to == null
                || !transaction.getCreatedAt().isAfter(to))
            .filter(transaction -> type == null
                || type.includes(transaction.getType()))
            .filter(transaction -> range == null
                || range.includes(transaction.getAmount()))
            .map(this::toTransactionResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse getActivityDetail(
        UUID accountId,
        UUID transactionId
    ) {
        Account account = findById(accountId);
        validateAccountAccess(account);

        Transaction transaction = transactionRepository
            .findByIdAndAccount_Id(transactionId, accountId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Movimiento no encontrado")
            );

        return toTransactionResponse(transaction);
    }

    public TransactionResponse deposit(
        UUID accountId,
        CreateDepositRequest request
    ) {
        Account account = findById(accountId);
        validateAccountAccess(account);

        PaymentCard card = paymentCardRepository
            .findByIdAndAccount_Id(request.cardId(), accountId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Tarjeta no encontrada")
            );

        account.credit(request.amount());

        Transaction transaction = new Transaction(
            account,
            TransactionType.DEPOSIT,
            request.amount(),
            "Ingreso de dinero con tarjeta terminada en "
                + card.getLastFour()
        );

        return toTransactionResponse(transactionRepository.save(transaction));
    }

    private Account findById(UUID accountId) {
        return accountRepository.findById(accountId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Cuenta no encontrada")
            );
    }

    private void validateAccountAccess(Account account) {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(authority ->
                authority.getAuthority().equals("ROLE_ADMIN")
            );

        boolean isOwner = account.getUserId().toString()
            .equals(authentication.getName());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(
                "No tenés permiso para acceder a esta cuenta"
            );
        }
    }


    private TransactionResponse toTransactionResponse(
        Transaction transaction
    ) {
        return new TransactionResponse(
            transaction.getId(),
            transaction.getAccount().getId(),
            transaction.getType(),
            transaction.getAmount().setScale(2),
            transaction.getDescription(),
            transaction.getCreatedAt()
        );
    }

    private String generateUniqueCvu() {
        String cvu;

        do {
            StringBuilder builder = new StringBuilder(22);

            for (int i = 0; i < 22; i++) {
                builder.append(secureRandom.nextInt(10));
            }

            cvu = builder.toString();
        } while (accountRepository.existsByCvu(cvu));

        return cvu;
    }

    private String generateUniqueAlias() {
        String alias;

        do {
            alias = String.join(
                ".",
                aliasWordsProvider.randomWord(secureRandom),
                aliasWordsProvider.randomWord(secureRandom),
                aliasWordsProvider.randomWord(secureRandom)
            );
        } while (accountRepository.existsByAlias(alias));

        return alias;
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
            account.getId(),
            account.getUserId(),
            account.getCvu(),
            account.getAlias(),
            account.getBalance().setScale(2),
            account.getHolderName()
        );
    }
}
