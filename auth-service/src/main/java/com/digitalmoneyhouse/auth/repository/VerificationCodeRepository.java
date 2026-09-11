package com.digitalmoneyhouse.auth.repository;

import com.digitalmoneyhouse.auth.domain.VerificationCode;
import com.digitalmoneyhouse.auth.domain.VerificationPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VerificationCodeRepository
    extends JpaRepository<VerificationCode, UUID> {

    List<VerificationCode> findByEmailIgnoreCaseAndPurposeAndUsedAtIsNull(
        String email,
        VerificationPurpose purpose
    );

    Optional<VerificationCode>
        findTopByEmailIgnoreCaseAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(
            String email,
            VerificationPurpose purpose
        );
}