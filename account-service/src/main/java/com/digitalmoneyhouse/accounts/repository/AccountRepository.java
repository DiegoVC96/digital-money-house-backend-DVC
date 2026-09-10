package com.digitalmoneyhouse.accounts.repository;

import com.digitalmoneyhouse.accounts.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository
    extends JpaRepository<Account, UUID> {

    Optional<Account> findByUserId(UUID userId);

    boolean existsByCvu(String cvu);

    boolean existsByAlias(String alias);
}