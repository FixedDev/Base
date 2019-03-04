package us.sparknetwork.base.api.module;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.bukkit.plugin.java.JavaPlugin;
import us.sparknetwork.base.api.inject.ProtectedModule;

import java.io.File;

@Singleton
public abstract class BaseModule extends ProtectedModule implements ModuleInfo {

    @Provides
    @Named("module-datafolder")
    private File provideDataFolder(JavaPlugin plugin){
        return new File(plugin.getDataFolder(), name());
    }

    public void onEnable() {
    }

    public void onDisable() {
    }
}
