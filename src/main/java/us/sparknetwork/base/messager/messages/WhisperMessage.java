package us.sparknetwork.base.messager.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class WhisperMessage {
    private UUID sender;
    private String senderName;
    private String senderNick;
    private String senderServerId;

    private UUID receiver;

    private String message;
}
