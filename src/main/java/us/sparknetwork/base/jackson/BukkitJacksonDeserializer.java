package us.sparknetwork.base.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.io.IOException;
import java.util.Map;

/**
 * Taken from https://variable.co.il/Voigon/jackson-datatype-bukkit/blob/master/src/main/java/net/voigon/jackson/bukkitserializers/deser/BukkitDeserializer.java
 * @param <T> a class that implements ConfigurationSerializable
 */
public class BukkitJacksonDeserializer<T extends ConfigurationSerializable> extends StdDeserializer<T> {

    private static final long serialVersionUID = 7153233793837350295L;

    private final Class<T> clazz;

    public BukkitJacksonDeserializer(Class<T> clazz) {
        super(clazz);
        this.clazz = clazz;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        @SuppressWarnings("rawtypes")
        Map map = ctxt.readValue(p, Map.class);

        return (T) ConfigurationSerialization.deserializeObject(map, clazz);
    }

}
