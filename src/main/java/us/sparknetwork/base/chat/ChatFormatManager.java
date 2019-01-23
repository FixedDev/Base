package us.sparknetwork.base.chat;

import org.bukkit.entity.Player;
import us.sparknetwork.base.Service;

import java.util.Set;

public interface ChatFormatManager extends Service {

    default ChatFormat getChatFormatForPlayer(Player player) {
        return getChatFormatForPlayer(player, PriorityChecking.LOWER_FIRST);
    }

    ChatFormat getChatFormatForPlayer(Player player, PriorityChecking priorityChecking);

    Set<ChatFormat> getRegisteredChatFormats();

    void registerChatFormat(ChatFormat chatFormat);

    void start();

    void stop();
}
