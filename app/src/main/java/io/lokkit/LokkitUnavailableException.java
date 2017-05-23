package io.lokkit;

/**
 * Created by Nick on 23.05.2017.
 */

public class LokkitUnavailableException extends LokkitException {

    public LokkitUnavailableException(String message) {
        super(message);
    }

    public LokkitUnavailableException(Throwable cause) {
        super("Failed to connect to lokkit", cause);
    }

    public LokkitUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
