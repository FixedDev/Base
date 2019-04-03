package us.sparknetwork.base.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Taken from https://variable.co.il/Voigon/jackson-datatype-bukkit/blob/master/src/main/java/net/voigon/jackson/bukkitserializers/ser/BukkitSerializer.java
 * @param <T> a class that implements ConfigurationSerializable
 */
public class BukkitJacksonSerializer<T extends ConfigurationSerializable> extends StdSerializer<T> {

    private static final long serialVersionUID = -6285671717773548614L;

    private Set<String> ignoreEntries = new HashSet<>();

    public BukkitJacksonSerializer(Class<T> clazz, Set<String> ignoreEntries) {
        super(clazz);
        this.ignoreEntries = ignoreEntries == null ? new HashSet<>() : ignoreEntries;
    }

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        ObjectMapper mapper = (ObjectMapper) gen.getCodec();

        Set<String> remove = new HashSet<>();

        Map<String, Object> map = value.serialize();
        if (!ignoreEntries.isEmpty()) {
            for (String entry : ignoreEntries) {
                remove.add(entry);
            }
        }

        for (String key : map.keySet()) {
            if (!remove.contains(key) && (key == null || map.get(key) == null)) {
                remove.add(key);
            }
        }

        remove.forEach(map::remove);

        mapper.writeValue(gen, value.serialize());
    }

}
