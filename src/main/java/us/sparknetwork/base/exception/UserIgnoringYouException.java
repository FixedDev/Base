

package us.sparknetwork.base.exception;

import lombok.AllArgsConstructor;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class UserIgnoringYouException extends Exception {
    private UUID userId;
    private String userNick;

    public UserIgnoringYouException(UUID userId) {
        this.userId = userId;
    }

    public UserIgnoringYouException(String userNick) {
        this.userNick = userNick;
    }

    @Nullable
    public Optional<UUID> getUserId() {
        return Optional.ofNullable(userId);
    }

    @Nullable
    public Optional<String> getUserNick() {
        return Optional.ofNullable(userNick);
    }
}
