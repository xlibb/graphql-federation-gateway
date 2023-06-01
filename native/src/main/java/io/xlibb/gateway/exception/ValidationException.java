package io.xlibb.gateway.exception;

/**
 * Exception class to represent validation errors.
 */
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }
}
