package us.sparknetwork.base;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import us.sparknetwork.base.inject.annotations.PluginClassLoader;
import us.sparknetwork.base.inject.annotations.PluginDataFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class I18n {

    private static final String RESOURCE_BUNDLE_NAME = "messages/messages";

    private static final ResourceBundle NULL_BUNDLE = new ResourceBundle() {
        @Override
        protected Object handleGetObject(@NotNull String key) {
            return null;
        }

        @Override
        @NotNull
        public Enumeration<String> getKeys() {
            return new Vector<String>().elements();
        }

    };

    private ResourceBundle defaultBundle;
    private ResourceBundle resourceBundle;
    private ResourceBundle customBundle;

    @Getter
    private Locale currentLocale;

    private Map<String, MessageFormat> cachedFormats = new HashMap<>();

    private File dataFolder;
    private Logger logger;
    private ClassLoader classLoader;

    private JavaPlugin plugin;

    @Inject
    public I18n(Plugin plugin, PluginLogger logger) {
        this.dataFolder = plugin.getDataFolder();
        this.logger = logger;
        this.classLoader = plugin.getClass().getClassLoader();

        // Just for "reload" support
        ResourceBundle.clearCache();

        try {
            defaultBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, Locale.ENGLISH, classLoader);
        } catch (MissingResourceException ex) {
            logger.warning("Failed to load default bundle");
            defaultBundle = NULL_BUNDLE;
        }

        try {
            String[] localeParts = ServerConfigurations.LANGUAGE.split("_");

            if (localeParts.length == 3) {
                currentLocale = new Locale(localeParts[0], localeParts[1], localeParts[2]);
            } else if (localeParts.length == 2) {
                currentLocale = new Locale(localeParts[0], localeParts[1]);
            } else if (localeParts.length == 1) {
                currentLocale = new Locale(localeParts[0]);
            }

            resourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, currentLocale, classLoader);
        } catch (MissingResourceException ex) {
            logger.warning("Failed to load locale bundle");
            resourceBundle = NULL_BUNDLE;
        }

        try {
            customBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, currentLocale, new ResourceClassLoader(I18n.class.getClassLoader(), dataFolder));
        } catch (MissingResourceException ex) {
            customBundle = NULL_BUNDLE;
        }
    }

    public void setCurrentLocale(Locale currentLocale) {
        if (currentLocale == null) throw new IllegalArgumentException("The locale is null or invalid");

        ResourceBundle.clearCache();

        this.currentLocale = currentLocale;
        this.resourceBundle = ResourceBundle.getBundle("messages", currentLocale);
        customBundle = ResourceBundle.getBundle("message", currentLocale, new ResourceClassLoader(I18n.class.getClassLoader(), dataFolder));
    }

    public void updateLocale(String locale) {
        if (!StringUtils.isNotBlank(locale)) throw new IllegalArgumentException("The locale is null or empty");
        String[] localeParts = locale.split("[_]");

        if (localeParts.length == 3) {
            currentLocale = new Locale(localeParts[0], localeParts[1], localeParts[2]);
        } else if (localeParts.length == 2) {
            currentLocale = new Locale(localeParts[0], localeParts[1]);
        } else if (localeParts.length == 1) {
            currentLocale = new Locale(localeParts[0]);
        }

    }

    public String translate(String path) {
        String translatedString;

        try {
            try {
                translatedString = customBundle.getString(path);
            } catch (MissingResourceException e) {
                try {
                    translatedString = resourceBundle.getString(path);
                } catch (MissingResourceException ex) {
                    translatedString = defaultBundle.getString(path);
                }
            }
        } catch (MissingResourceException e) {
            logger.log(Level.SEVERE, "Failed to get translation text: {0}", path);
            return "";
        }

        translatedString = ChatColor.translateAlternateColorCodes('&', translatedString);

        return translatedString;
    }

    public String format(String path, Object... params) {
        String translatedText = translate(path);

        MessageFormat formatter = cachedFormats.get(translatedText);

        if (formatter == null) {
            try {
                formatter = new MessageFormat(translatedText);
            } catch (IllegalArgumentException e) {
                logger.log(Level.WARNING, "Invalid text translation: {0}", translatedText);

                // taken from essentials :3
                translatedText = translatedText.replaceAll("\\{(\\D*?)\\}", "\\[$1\\]");
                formatter = new MessageFormat(translatedText);
            }

            cachedFormats.put(translatedText, formatter);
        }

        return formatter.format(params);
    }

    private static class ResourceClassLoader extends ClassLoader {
        private final File dataFolder;

        ResourceClassLoader(final ClassLoader classLoader, final File dataFolder) {
            super(classLoader);
            if (!dataFolder.isDirectory()) throw new IllegalArgumentException("The data folder isn't a directory!");
            this.dataFolder = dataFolder;
        }

        @Override
        public URL getResource(final String string) {

            final File file = new File(dataFolder, string);
            if (file.exists()) {
                try {
                    return file.toURI().toURL();
                } catch (MalformedURLException ex) {
                }
            }
            return null;
        }

        @Override
        public InputStream getResourceAsStream(final String string) {
            final File file = new File(dataFolder, string);
            if (file.exists()) {
                try {

                    return new FileInputStream(file);
                } catch (FileNotFoundException ex) {
                }
            }

            return null;
        }
    }
}
