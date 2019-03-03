

package us.sparknetwork.base.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class UserMessagesDisabledException extends Exception {
    private UUID userId;
    private String userNick;

    public UserMessagesDisabledException(UUID userId) {
        this.userId = userId;
    }

    public UserMessagesDisabledException(String userNick) {
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
