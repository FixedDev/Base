package us.sparknetwork.base.module;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.bukkit.plugin.java.JavaPlugin;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.inject.annotations.ModuleDataFolder;
import us.sparknetwork.base.inject.annotations.ModuleI18n;
import us.sparknetwork.utils.inject.ProtectedModule;

import java.io.File;

@Singleton
public abstract class BaseModule extends ProtectedModule implements ModuleInfo {

    @Provides
    @ModuleDataFolder
    private File provideDataFolder(JavaPlugin plugin){
        return new File(plugin.getDataFolder(), name());
    }

    @Override
    protected final void configure() {
        bind(I18n.class).annotatedWith(ModuleI18n.class).to(us.sparknetwork.base.module.ModuleI18n.class);
        bindings();
    }

    protected abstract void bindings();

    public void onEnable() {
    }

    public void onDisable() {
    }
}
