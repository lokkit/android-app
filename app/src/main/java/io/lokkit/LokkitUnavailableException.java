package io.lokkit;

/**
 * This exception is thrown when the predefined lokkit network cannot be reached.
 */

class LokkitUnavailableException extends LokkitException {

    public LokkitUnavailableException(String message) {
        super(message);
    }

    public LokkitUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
