package com.digitalmoneyhouse.auth.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "verification_codes")
public class VerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 100)
    private String codeHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VerificationPurpose purpose;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant usedAt;

    protected VerificationCode() {
    }

    public VerificationCode(
        String email,
        String codeHash,
        VerificationPurpose purpose,
        Instant expiresAt
    ) {
        this.email = email;
        this.codeHash = codeHash;
        this.purpose = purpose;
        this.createdAt = Instant.now();
        this.expiresAt = expiresAt;
    }

    public String getEmail() {
        return email;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public VerificationPurpose getPurpose() {
        return purpose;
    }

    public boolean isActive(Instant now) {
        return usedAt == null && expiresAt.isAfter(now);
    }

    public void markAsUsed() {
        usedAt = Instant.now();
    }
}