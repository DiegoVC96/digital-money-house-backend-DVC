package com.digitalmoneyhouse.accounts.service;

import com.digitalmoneyhouse.accounts.api.AccountDtos.AccountResponse;
import com.digitalmoneyhouse.accounts.api.AccountDtos.CreateAccountRequest;
import com.digitalmoneyhouse.accounts.domain.Account;
import com.digitalmoneyhouse.accounts.repository.AccountRepository;
import com.digitalmoneyhouse.common.exception.ConflictException;
import com.digitalmoneyhouse.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.UUID;

@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final AliasWordsProvider aliasWordsProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    public AccountService(
        AccountRepository accountRepository,
        AliasWordsProvider aliasWordsProvider
    ) {
        this.accountRepository = accountRepository;
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