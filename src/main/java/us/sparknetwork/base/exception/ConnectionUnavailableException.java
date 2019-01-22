package us.sparknetwork.base.exception;

public class ConnectionUnavailableException extends RuntimeException {

    public ConnectionUnavailableException() {
    }

    public ConnectionUnavailableException(String message) {
        super(message);
    }

    public ConnectionUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectionUnavailableException(Throwable cause) {
        super(cause);
    }
}
