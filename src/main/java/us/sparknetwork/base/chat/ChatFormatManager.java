package us.sparknetwork.base.chat;

import org.bukkit.entity.Player;
import us.sparknetwork.base.Service;

import java.util.List;

public interface ChatFormatManager extends Service {

    default ChatFormat getChatFormatForPlayer(Player player) {
        return getChatFormatForPlayer(player, PriorityChecking.LOWER_FIRST);
    }

    ChatFormat getChatFormatForPlayer(Player player, PriorityChecking priorityChecking);

    List<ChatFormat> getRegisteredChatFormats();

    void registerChatFormat(ChatFormat chatFormat);

    void reload();
}
