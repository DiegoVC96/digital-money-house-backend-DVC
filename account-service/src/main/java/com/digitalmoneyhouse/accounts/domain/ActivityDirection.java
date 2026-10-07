package com.digitalmoneyhouse.accounts.domain;

public enum ActivityDirection {
    INCOME,
    EXPENSE;

    public boolean includes(TransactionType transactionType) {
        return switch (this) {
            case INCOME -> transactionType == TransactionType.DEPOSIT
                || transactionType == TransactionType.TRANSFER_IN;
            case EXPENSE -> transactionType == TransactionType.TRANSFER_OUT;
        };
    }
}
