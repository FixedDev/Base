package us.sparknetwork.base.datamanager.jackson;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import us.sparknetwork.base.datamanager.Model;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Ben Fagin
 * @version 2013-12-13
 */
public class ModelMapper {

    @SuppressWarnings("unchecked")
    public static <T> T map(Class<T> klaus, final ClassLoader classLoader, final ObjectNode data) {
        checkNotNull(data);
        checkArgument(klaus != null && klaus.isInterface(), "interfaces only please");

        return (T) Proxy.newProxyInstance(classLoader, new Class[]{klaus}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String property = method.getName().substring(3);
                property = new String(new char[]{property.charAt(0)}).toLowerCase() + property.substring(1);

                // getter
                if ((method.getName().startsWith("get") || method.getName().startsWith("is")) && method.getParameterTypes().length == 0) {
                    return getProperty(data, property, method, classLoader);
                }

                // setter
                if (method.getName().startsWith("set") && method.getParameterTypes().length == 1) {
                    data.putPOJO(property, args[0]);
                    return null;
                }

                boolean accesible = method.isAccessible();
                method.setAccessible(true);

                try {
                    return method.invoke(property, args);
                } catch (AbstractMethodError ex) {
                    throw new RuntimeException("Not a property: " + method.getName());
                } finally {
                    method.setAccessible(accesible);
                }
            }
        });
    }

    private static Object getProperty(ObjectNode data, String property, Method method, ClassLoader classLoader) {
        Class<?> returnType = method.getReturnType();
        JsonNode value = data.get(property);

        if (Integer.class.equals(returnType) || int.class.equals(returnType)) {
            return value.asInt();
        } else if (Double.class.equals(returnType) || double.class.equals(returnType)) {
            return value.asDouble();
        } else if (Long.class.equals(returnType) || long.class.equals(returnType)) {
            return value.asLong();
        } else if (Boolean.class.equals(returnType) || boolean.class.equals(returnType)) {
            return value.asBoolean();
        } else if (String.class.equals(returnType)) {
            return value.asText();
        } else if (returnType.isInterface() && Model.class.isAssignableFrom(returnType)) {
            if (value.isObject()) {
                return map(returnType, classLoader, (ObjectNode) value);
            } else if (value.isPojo()) {
                return ((POJONode) value).getPojo();
            }
        }

        throw new RuntimeException("Unknown return type: " + returnType);
    }
}