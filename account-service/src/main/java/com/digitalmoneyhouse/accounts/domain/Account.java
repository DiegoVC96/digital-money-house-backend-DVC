package com.digitalmoneyhouse.accounts.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false, unique = true, length = 22)
    private String cvu;

    @Column(nullable = false, unique = true, length = 80)
    private String alias;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, length = 160)
    private String holderName;

    protected Account() {
    }

    public Account(
        UUID userId,
        String cvu,
        String alias,
        String holderName
    ) {
        this.userId = userId;
        this.cvu = cvu;
        this.alias = alias;
        this.holderName = holderName;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getCvu() {
        return cvu;
    }

    public String getAlias() {
        return alias;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getHolderName() {
        return holderName;
    }

    public void updateAlias(String alias) {
        this.alias = alias;
    }
}