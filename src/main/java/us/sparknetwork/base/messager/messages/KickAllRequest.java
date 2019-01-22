package us.sparknetwork.base.messager.messages;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor @Data
public class KickAllRequest {
    private final String kicker;
    private final String reason;
    private final String bypassPermission;
}
