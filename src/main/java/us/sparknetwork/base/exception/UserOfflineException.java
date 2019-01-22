package us.sparknetwork.base.exception;

import lombok.AllArgsConstructor;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class UserOfflineException extends Exception {
    private UUID userId;
    private String userNick;

    public UserOfflineException(UUID userId) {
        this.userId = userId;
    }

    public UserOfflineException(String userNick) {
        this.userNick = userNick;
    }

    public Optional<UUID> getUserId() {
        return Optional.ofNullable(userId);
    }

    public Optional<String> getUserNick() {
        return Optional.ofNullable(userNick);
    }

}
