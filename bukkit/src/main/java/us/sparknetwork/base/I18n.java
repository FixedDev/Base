package us.sparknetwork.base;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;

@Singleton
public class I18n {

    private static final String resourceBundleName = "messages/messages";

    private static final ResourceBundle NULL_BUNDLE = new ResourceBundle() {
        @Override
        protected Object handleGetObject(String key) {
            return null;
        }

        @Override
        public Enumeration<String> getKeys() {
            return null;
        }

    };

    private ResourceBundle defaultBundle;
    private ResourceBundle resourceBundle;
    private ResourceBundle customBundle;

    @Getter
    private Locale currentLocale;

    private Map<String, MessageFormat> cachedFormats = new HashMap<>();

    private JavaPlugin plugin;

    @Inject
    public I18n(JavaPlugin plugin) {
        this.plugin = plugin;

        // Just for "reload" support
        ResourceBundle.clearCache();

        try {
            defaultBundle = ResourceBundle.getBundle(resourceBundleName, Locale.ENGLISH);
        } catch (MissingResourceException ex) {
            plugin.getLogger().warning("Failed to load default bundle");
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

            resourceBundle = ResourceBundle.getBundle(resourceBundleName, currentLocale);
        } catch (MissingResourceException ex) {
            plugin.getLogger().warning("Failed to load locale bundle");
            resourceBundle = NULL_BUNDLE;
        }

        try {
            customBundle = ResourceBundle.getBundle(resourceBundleName, currentLocale, new ResourceClassLoader(I18n.class.getClassLoader(), plugin.getDataFolder()));
        } catch (MissingResourceException ex) {
            customBundle = NULL_BUNDLE;
        }
    }

    public void setCurrentLocale(Locale currentLocale) {
        if (currentLocale == null) throw new IllegalArgumentException("The locale is null or invalid");

        ResourceBundle.clearCache();

        this.currentLocale = currentLocale;
        this.resourceBundle = ResourceBundle.getBundle("messages", currentLocale);
        customBundle = ResourceBundle.getBundle("message", currentLocale, new ResourceClassLoader(I18n.class.getClassLoader(), plugin.getDataFolder()));
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
            plugin.getLogger().log(Level.SEVERE, "Failed to get translation text: {0}", path);
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
                plugin.getLogger().log(Level.WARNING, "Invalid text translation: {0}", translatedText);

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
