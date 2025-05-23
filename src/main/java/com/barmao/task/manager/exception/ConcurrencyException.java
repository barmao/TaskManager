package com.barmao.task.manager.exception;

public class ConcurrencyException extends RuntimeException {

    public ConcurrencyException(String message) {
        super(message);
    }

    public ConcurrencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
