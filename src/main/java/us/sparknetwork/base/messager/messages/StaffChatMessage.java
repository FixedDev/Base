package us.sparknetwork.base.messager.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class StaffChatMessage {
    private String senderNick;
    private String message;
}
