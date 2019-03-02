package us.sparknetwork.base.announcer;

import lombok.Getter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Map;

@Getter
public class Announce implements ConfigurationSerializable {

    private @NotNull String message;
    private @NotNull Duration announcePeriod;
    private @Nullable String permission;

    public Announce(@NotNull String message, @NotNull Duration announcePeriod, @Nullable String permission) {
        this.message = message;
        this.announcePeriod = announcePeriod;
        this.permission = permission;
    }

    public Announce(String message, Duration announcePeriod) {
        this(message, announcePeriod, null);
    }

    @Override
    public Map<String, Object> serialize() {
        return null;
    }
}
