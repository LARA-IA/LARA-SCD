package com.lara.scd.exception;

public class UnicidadeVioladaException extends RuntimeException {

    public UnicidadeVioladaException(String message) {
        super(message);
    }

    public UnicidadeVioladaException(String message, Throwable cause) {
        super(message, cause);
    }
}