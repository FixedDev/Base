package us.sparknetwork.base.announcer;

import lombok.Getter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Map;

@Getter
public class Announce implements ConfigurationSerializable {
    private String message;

    public Announce(String message) {
        this.message = message;

    }

    @Override
    public Map<String, Object> serialize() {
        return null;
    }
}
