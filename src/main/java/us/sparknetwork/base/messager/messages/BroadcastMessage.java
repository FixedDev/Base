package us.sparknetwork.base.messager.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BroadcastMessage {
    private BroadcastType type;
    private String message;

    public enum BroadcastType {
        RAW, NORMAL
    }
}
