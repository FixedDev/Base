package us.sparknetwork.base.datamanager.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.node.ObjectNode;
import us.sparknetwork.base.datamanager.Model;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author Ben Fagin
 * @version 2013-12-13
 */
public class MapperModule extends Module {

    @Override
    public String getModuleName() {
        return "ModelMapper";
    }

    @Override
    public Version version() {
        return new Version(2, 0, 0, null, null, null);
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addDeserializers(new Deserializers.Base() {
            public @Override
            JsonDeserializer<?> findBeanDeserializer(final JavaType type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
                if (Model.class.isAssignableFrom(type.getRawClass())) {
                    return new JsonDeserializer<Object>() {
                        public @Override
                        Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                            Iterator<ObjectNode> obj = jp.readValuesAs(ObjectNode.class);
                            return ModelMapper.map(type.getRawClass(), context.getTypeFactory().getClassLoader(), obj.next());
                        }
                    };
                }

                return super.findBeanDeserializer(type, config, beanDesc);
            }
        });
    }
}