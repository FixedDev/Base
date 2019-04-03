package us.sparknetwork.base.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.bukkit.inventory.ItemStack;

public class BukkitJacksonModule extends SimpleModule {
    public BukkitJacksonModule() {
        addSerializer(new BukkitJacksonSerializer<>(ItemStack.class, null))
                .addDeserializer(ItemStack.class, new BukkitJacksonDeserializer<>(ItemStack.class));
    }
}
