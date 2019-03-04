package us.sparknetwork.base.chat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import us.sparknetwork.utils.Config;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class BaseChatFormatManager implements ChatFormatManager {

    private AtomicBoolean started;

    private List<ChatFormat> chatFormats;

    private ChatFormat defaultFormat;

    private JavaPlugin plugin;

    private Config chatConfig;

    @Inject
    BaseChatFormatManager(JavaPlugin plugin) {
        this.plugin = plugin;

        ConfigurationSerialization.registerClass(ChatFormat.class);
        ConfigurationSerialization.registerClass(BaseChatFormat.class);

        chatConfig = new Config(plugin, "chat");

        chatFormats = new CopyOnWriteArrayList<>();

        started = new AtomicBoolean();
    }

    @Override
    public ChatFormat getChatFormatForPlayer(Player player, PriorityChecking priorityChecking) {
        Stream<ChatFormat> chatFormatStream = chatFormats.stream()
                .filter(format -> player.hasPermission(format.getPermission()));

        List<ChatFormat> sortedChatFormats;

        switch (priorityChecking) {
            case LOWER_FIRST:
                sortedChatFormats = chatFormatStream.sorted((o1, o2) -> Integer.compare(o1.getPriority(), o2.getPriority())).collect(Collectors.toList());
                break;
            case HIGHER_FIRST:
                sortedChatFormats = chatFormatStream.sorted((o1, o2) -> Integer.compare(o2.getPriority(), o1.getPriority())).collect(Collectors.toList());
                break;
            default: // In case that priorityChecking is null return random order
                sortedChatFormats = chatFormatStream.collect(Collectors.toList());
        }

        for (ChatFormat chatFormat : sortedChatFormats) {
            if (chatFormat == null) {
                continue;
            }

            return chatFormat;
        }

        return defaultFormat;
    }

    @Override
    public List<ChatFormat> getRegisteredChatFormats() {
        return chatFormats;
    }

    @Override
    public void registerChatFormat(ChatFormat chatFormat) {
        chatFormats.add(chatFormat);
    }

    @Override
    public void reload() {
        chatConfig = new Config(plugin,"config");

        loadConfig();
    }

    @Override
    public void start() {
        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException("The service is already started");
        }

        loadConfig();
    }

    @Override
    public void stop() {
        if (!started.compareAndSet(true, false)) {
            throw new IllegalStateException("The service isn't already started");
        }

        chatConfig.set("formats", chatFormats);
        chatConfig.save();
    }

    @Override
    public boolean isStarted() {
        return started.get();
    }

    private void loadConfig(){
        List<?> formatsRawList = chatConfig.getList("formats");

        List<ChatFormat> formats = new ArrayList<>();

        formatsRawList.forEach(o -> {
            if (!(o instanceof ChatFormat)) {
                return;
            }

            ChatFormat format = (ChatFormat) o;

            if (format.isUsePlaceholderApi() && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
                plugin.getLogger().log(Level.WARNING, "The format with name {0} has PlaceholderAPI enabled but PlaceholderAPI isn't installed, not loading it.", format.getFormatName());
                return;
            }

            if (format.isAllowRelationalPlaceholders() && !format.isUsePlaceholderApi()) {
                plugin.getLogger().log(Level.WARNING, "The format with name {0} has Relational Placeholders enabled but it doesn't has enabled the PlaceholderAPI support, ignoring Relational Placeholders.");
            }

            formats.add(format);
        });

        this.chatFormats = formats;

        Optional<ChatFormat> optionalDefaultFormat = formats.stream().filter(chatFormat -> chatFormat.getFormatName().equalsIgnoreCase("default")).findFirst();

        if (!optionalDefaultFormat.isPresent()) {
            plugin.getLogger().log(Level.INFO, "Default chat format not exists, creating it.");

            ChatFormat format = new BaseChatFormat("default", 999999);
            defaultFormat = format;

            chatFormats.add(format);

            chatConfig.set("formats", chatFormats);
            chatConfig.save();

            return;
        }

        defaultFormat = optionalDefaultFormat.get();
    }
}
