package us.sparknetwork.base.user.friends;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
public class FriendRequestReply {
    @Nullable
    private final FriendRequest request;

    @NotNull
    private final RequestReply requestReply;

    public enum RequestReply {
        ACCEPTED, DENIED, NOT_FOUND
    }
}
