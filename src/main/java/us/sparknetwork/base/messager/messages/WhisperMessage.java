package us.sparknetwork.base.messager.messages;

import us.sparknetwork.base.handlers.user.Identity;

public class WhisperMessage {
    private Identity from;
    private Identity to;
    private String message;

    public WhisperMessage(Identity from, Identity to, String message) {
        this.from = from;
        this.to = to;
        this.message = message;
    }

    public Identity getFrom() {
        return this.from;
    }

    public Identity getTo() {
        return this.to;
    }

    public String getMessage() {
        return this.message;
    }
}
