package us.sparknetwork.base;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.PluginClassLoader;
import us.sparknetwork.utils.DependencyDownloader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class BasePlugin extends JavaPlugin {

    public static final UUID CONSOLE_UUID = UUID.fromString("fecac143-508a-4c25-845c-dab5b9b4a03f");

    private ListeningExecutorService executorService;

    private BasePluginLoader pluginLoader;

    public static void logError(Logger logger, String action, String dataType, String dataId, Throwable error) {
        Preconditions.checkNotNull(logger);

        Preconditions.checkNotNull(action);
        Preconditions.checkArgument(!action.isEmpty());

        Preconditions.checkNotNull(dataType);
        Preconditions.checkArgument(!dataType.isEmpty());


        String message = StringUtils.isBlank(dataId) ? "Failed to {0} the {1}" : "Failed to {0} the {1} of {2}";

        logger.log(new LogRecord(Level.WARNING, message) {

            @Override
            public Object[] getParameters() {
                if (dataId == null) {
                    return new Object[]{action, dataType};
                }
                return new Object[]{action, dataType, dataId};
            }

            @Override
            public Throwable getThrown() {
                return error;
            }
        });
    }

    static {
        try {
            DependencyDownloader.addFolderJarsToClassPath(new File("lib"), PluginClassLoader.getSystemClassLoader());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | MalformedURLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to load plugin dependencies", e);
        }
    }

    @Override
    public void onLoad() {
        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdir();
        }

        executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));




        pluginLoader = new BasePluginLoader(this, executorService);
    }


    @Override
    public void onEnable() {
        if (pluginLoader != null) {
            pluginLoader.onEnable();
        }
    }

    @Override
    public void onDisable() {
        if (pluginLoader != null) {
            pluginLoader.onDisable();
        }
    }


    void setPluginEnabled(boolean enabled) {
        this.setEnabled(enabled);
    }

    ClassLoader getPluginClassLoader() {
        return getClassLoader();
    }
}
