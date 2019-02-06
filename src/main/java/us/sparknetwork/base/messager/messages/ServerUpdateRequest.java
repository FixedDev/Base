package us.sparknetwork.base.messager.messages;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ServerUpdateRequest {
    private final String serverId;
}
