package us.sparknetwork.base.messager.messages;

import us.sparknetwork.base.user.Identity;

import java.beans.ConstructorProperties;

public class WhisperMessage {
    private Identity from;
    private Identity to;
    private String message;

    @ConstructorProperties({"from", "to", "message"})
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
