package us.sparknetwork.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Config extends YamlConfiguration {
    private final String fileName;
    private final JavaPlugin plugin;
    private final File folder;

    public Config(final JavaPlugin plugin, final String fileName) {
        this(plugin, plugin.getDataFolder(), fileName, ".yml");
    }

    public Config(final JavaPlugin plugin, final File folder, final String fileName, final String fileExtension) {
        this.plugin = plugin;
        this.folder = folder;
        this.fileName = fileName + (fileName.endsWith(fileExtension) ? "" : fileExtension);
        this.createFile();
    }

    public String getFileName() {
        return this.fileName;
    }

    public JavaPlugin getPlugin() {
        return this.plugin;
    }

    private void createFile() {
        try {
            final File file = new File(folder, this.fileName);
            if (!file.exists()) {
                if (this.plugin.getResource(this.fileName) != null) {
                    this.plugin.saveResource(this.fileName, false);
                } else {
                    this.save(file);
                }
                this.load(file);
            } else {
                this.load(file);
                this.save(file);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void save() {
        try {
            this.save(new File(folder, this.fileName));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
