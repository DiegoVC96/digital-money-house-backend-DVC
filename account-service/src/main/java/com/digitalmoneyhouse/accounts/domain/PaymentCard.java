package com.digitalmoneyhouse.accounts.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "payment_cards")
public class PaymentCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, unique = true, length = 64)
    private String fingerprint;

    @Column(nullable = false, length = 4)
    private String lastFour;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardBrand brand;

    @Column(nullable = false, length = 100)
    private String holderName;

    @Column(nullable = false, length = 5)
    private String expiration;

    protected PaymentCard() {
    }

    public PaymentCard(
        Account account,
        String fingerprint,
        String lastFour,
        CardBrand brand,
        String holderName,
        String expiration
    ) {
        this.account = account;
        this.fingerprint = fingerprint;
        this.lastFour = lastFour;
        this.brand = brand;
        this.holderName = holderName;
        this.expiration = expiration;
    }

    public UUID getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public String getLastFour() {
        return lastFour;
    }

    public CardBrand getBrand() {
        return brand;
    }

    public String getHolderName() {
        return holderName;
    }

    public String getExpiration() {
        return expiration;
    }
}