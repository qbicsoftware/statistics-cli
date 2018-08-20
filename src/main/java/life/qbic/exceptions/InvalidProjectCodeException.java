package life.qbic.exceptions;

import java.io.IOException;

public class InvalidProjectCodeException extends RuntimeException {

    public InvalidProjectCodeException(String message) {
        super(message);
    }

    public InvalidProjectCodeException(Throwable cause) {
        super(cause);
    }

    public InvalidProjectCodeException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
