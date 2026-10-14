package com.digitalmoneyhouse.accounts.repository;

import com.digitalmoneyhouse.accounts.domain.MoneyTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MoneyTransferRepository extends JpaRepository<MoneyTransfer, UUID> {

    List<MoneyTransfer> findBySourceAccount_IdOrderByCreatedAtDesc(UUID sourceAccountId);
}
