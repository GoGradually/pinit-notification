package me.pinitnotification.application.push.exception;

public class PushSendFailedException extends RuntimeException {
    public PushSendFailedException(String message) {
        super(message);
    }

    public PushSendFailedException(Throwable cause) {
        super(cause);
    }
}
