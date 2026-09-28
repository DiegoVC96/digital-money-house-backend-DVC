package com.digitalmoneyhouse.accounts.repository;

import com.digitalmoneyhouse.accounts.domain.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentCardRepository
    extends JpaRepository<PaymentCard, UUID> {

    List<PaymentCard> findByAccount_Id(UUID accountId);

    Optional<PaymentCard> findByIdAndAccount_Id(UUID cardId, UUID accountId);

    boolean existsByFingerprint(String fingerprint);
}