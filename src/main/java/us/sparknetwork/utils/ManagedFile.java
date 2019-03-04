package us.sparknetwork.utils;

import org.bukkit.plugin.java.JavaPlugin;
import us.sparknetwork.base.BasePlugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class ManagedFile {

    private File file;
    private JavaPlugin plugin;

    public ManagedFile(String filename, JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), filename);

        if (!this.file.exists()) {
            try {
                Files.copy(BasePlugin.class.getResourceAsStream(filename), file.toPath());
            } catch (IOException ex) {
                plugin.getLogger().log(Level.SEVERE, "items.csv has not been loaded", ex);
            }
        }
    }

    public File getFile() {
        return this.file;
    }

    public List<String> getLines() {
        try {
            return Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }

}