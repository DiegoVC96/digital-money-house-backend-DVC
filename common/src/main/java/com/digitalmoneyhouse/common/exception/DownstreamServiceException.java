package com.digitalmoneyhouse.common.exception;

public class DownstreamServiceException extends RuntimeException {

    private final int status;

    public DownstreamServiceException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
