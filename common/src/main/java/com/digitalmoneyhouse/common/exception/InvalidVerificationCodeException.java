package com.digitalmoneyhouse.common.exception;

public class InvalidVerificationCodeException
    extends RuntimeException {

    public InvalidVerificationCodeException(String message) {
        super(message);
    }
}