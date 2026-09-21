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

import java.security.SecureRandom;
import java.util.UUID;
import java.util.List;

@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final AliasWordsProvider aliasWordsProvider;
    private final SecureRandom secureRandom = new SecureRandom();
    private final TransactionRepository transactionRepository;

    public AccountService(
        AccountRepository accountRepository,
        TransactionRepository transactionRepository,
        AliasWordsProvider aliasWordsProvider
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
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