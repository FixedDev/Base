package us.sparknetwork.base.messager.messages;


import us.sparknetwork.base.user.User;

import java.beans.ConstructorProperties;

public class WhisperMessage {
    private User.Identity from;
    private User.Identity to;
    private String message;

    @ConstructorProperties({"from", "to", "message"})
    public WhisperMessage(User.Identity from, User.Identity to, String message) {
        this.from = from;
        this.to = to;
        this.message = message;
    }

    public User.Identity getFrom() {
        return this.from;
    }

    public User.Identity getTo() {
        return this.to;
    }

    public String getMessage() {
        return this.message;
    }
}
