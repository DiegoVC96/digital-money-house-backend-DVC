package com.digitalmoneyhouse.accounts.domain;

import java.math.BigDecimal;

public enum ActivityAmountRange {
    ZERO_TO_1000("0", "1000"),
    FROM_1000_TO_5000("1000", "5000"),
    FROM_5000_TO_20000("5000", "20000"),
    FROM_20000_TO_100000("20000", "100000"),
    OVER_100000("100000", null);

    private final BigDecimal minimum;
    private final BigDecimal maximum;

    ActivityAmountRange(String minimum, String maximum) {
        this.minimum = new BigDecimal(minimum);
        this.maximum = maximum == null ? null : new BigDecimal(maximum);
    }

    public boolean includes(BigDecimal amount) {
        return amount.compareTo(minimum) >= 0
            && (maximum == null || amount.compareTo(maximum) <= 0);
    }
}
