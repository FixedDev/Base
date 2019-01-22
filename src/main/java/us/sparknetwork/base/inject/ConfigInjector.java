package us.sparknetwork.base.inject;

import com.google.inject.MembersInjector;
import com.google.inject.name.Named;
import lombok.AllArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;
import us.sparknetwork.utils.Config;

import java.lang.reflect.Field;

@AllArgsConstructor
public class ConfigInjector<I> implements MembersInjector<I> {

    private Class<?> clazz;
    private Field field;
    private JavaPlugin plugin;

    @Override
    public void injectMembers(I i) {
        String configName = field.getAnnotation(Named.class).value();

        field.setAccessible(true);

        try {
            field.set(i, new Config(plugin, configName));
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to inject config instance at class " + clazz.getName(), e);
        }
    }
}
