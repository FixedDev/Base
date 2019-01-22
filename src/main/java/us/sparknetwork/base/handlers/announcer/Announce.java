package us.sparknetwork.base.handlers.announcer;

import lombok.Getter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import us.sparknetwork.base.datamanager.Model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
