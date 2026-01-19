package me.pinitnotification.application.push;

public record PushSendResult(
        boolean success,
        boolean invalidToken
) {
    public static PushSendResult successResult() {
        return new PushSendResult(true, false);
    }

    public static PushSendResult invalidTokenResult() {
        return new PushSendResult(false, true);
    }

    public static PushSendResult failedResult() {
        return new PushSendResult(false, false);
    }

    public boolean shouldDeleteToken() {
        return invalidToken;
    }
}
