package io.lokkit;

/**
 * Standard exception when Lokkit encountered a problem.
 */
public class LokkitException extends Exception {
    public LokkitException() {
        super();
    }

    public LokkitException(String message) {
        super(message);
    }

    public LokkitException(String message, Throwable cause) {
        super(message, cause);
    }
}
