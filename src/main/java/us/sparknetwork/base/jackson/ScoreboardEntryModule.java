package us.sparknetwork.base.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import me.ggamer55.scoreboard.entry.Entry;

import java.io.IOException;

public class ScoreboardEntryModule extends SimpleModule {
    public ScoreboardEntryModule() {
        addSerializer(Entry.class, new JsonSerializer<Entry>() {
            @Override
            public void serialize(Entry entry, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("entryName", entry.getEntryName());
                jsonGenerator.writeNumberField("updateTicks", entry.getUpdateTicks());

                jsonGenerator.writeArrayFieldStart("entries");
                for (String frame : entry.getFrames()) {
                    jsonGenerator.writeString(frame);
                }
                jsonGenerator.writeEndArray();
                jsonGenerator.writeEndObject();
            }
        });
    }
}
