package us.sparknetwork.base.chat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import us.sparknetwork.utils.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class BaseChatFormatManager implements ChatFormatManager {

    private Set<ChatFormat> chatFormats;

    @Inject
    @Named("chat")
    private Config chatConfig;

    @Inject
    private JavaPlugin plugin;

    BaseChatFormatManager() {
        this.chatFormats = ConcurrentHashMap.newKeySet();
        ConfigurationSerialization.registerClass(ChatFormat.class);
        ConfigurationSerialization.registerClass(BaseChatFormat.class);
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
            if(chatFormat == null){
                continue;
            }

            return chatFormat;
        }

        return null;
    }

    @Override
    public Set<ChatFormat> getRegisteredChatFormats() {
        return chatFormats;
    }

    @Override
    public void registerChatFormat(ChatFormat chatFormat) {
        chatFormats.add(chatFormat);
    }

    @Override
    public void loadFormats() {
        List<?> formatsRawList = chatConfig.getList("formats");

        List<ChatFormat> formats = new ArrayList<>();

        formatsRawList.forEach(o -> {
            if(!(o instanceof ChatFormat)){
                return;
            }

            ChatFormat format = (ChatFormat) o;

            if(format.isUsePlaceholderApi() && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null){
                plugin.getLogger().log(Level.WARNING, "The format with name {0} has PlaceholderAPI enabled but PlaceholderAPI isn't installed, not loading it.", format.getName());
                return;
            }

            if(format.isAllowRelationalPlaceholders() && !format.isUsePlaceholderApi()){
                plugin.getLogger().log(Level.WARNING, "The format with name {0} has Relational Placeholders enabled but it doesn't has enabled the PlaceholderAPI support, ignoring Relational Placeholders.");
            }

            formats.add(format);
        });
    }

    @Override
    public void saveFormats() {
        chatConfig.set("formats", chatFormats);
        chatConfig.save();
    }
}
