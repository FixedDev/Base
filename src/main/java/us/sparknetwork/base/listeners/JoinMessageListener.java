package us.sparknetwork.base.listeners;

import com.google.inject.Inject;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.handlers.user.state.UserState;
import us.sparknetwork.base.handlers.user.state.UserStateHandler;
import us.sparknetwork.utils.JsonMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static us.sparknetwork.utils.ListenableFutureUtils.*;

public class JoinMessageListener implements Listener {
    @Inject
    private I18n i18n;

    @Inject
    private UserStateHandler userStateHandler;

    @Inject
    private Chat chat;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String oldJoinMessage = event.getJoinMessage();

        Optional<UserState> optionalState = Optional.ofNullable(userStateHandler.findOneSync(event.getPlayer().getUniqueId().toString()));

        if(!optionalState.isPresent()){
            return;
        }

        UserState state = optionalState.get();

        boolean isUserVanished = state.isVanished();

        if (isUserVanished) {
            Bukkit.getConsoleSender().sendMessage(oldJoinMessage);
            event.setJoinMessage(null);
            return;
        }

        String joinMessage = i18n.translate("join.message");
        event.setJoinMessage(null);

        if (joinMessage.equalsIgnoreCase("{none}")) {
            return;
        }

        Bukkit.getConsoleSender().sendMessage(oldJoinMessage);

        joinMessage = joinMessage.replace("{name}", event.getPlayer().getName())
                .replace("{displayName}", event.getPlayer().getDisplayName())
                .replace("{prefix}", chat.getGroupPrefix((String) null, chat.getPrimaryGroup(event.getPlayer())));

        joinMessage = ChatColor.translateAlternateColorCodes('&', joinMessage);

        Collection<? extends Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        Player[] onlinePlayersArray = new Player[onlinePlayers.size()];

        JsonMessage.sendRawJson(joinMessage, onlinePlayers.toArray(onlinePlayersArray));


    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        String oldLeaveMessage = event.getQuitMessage();

        Optional<UserState> optionalState = Optional.ofNullable(userStateHandler.findOneSync(event.getPlayer().getUniqueId().toString()));

        if (!optionalState.isPresent()) {
            return;
        }

        UserState state = optionalState.get();

        boolean isUserVanished = state.isVanished();

        if (isUserVanished) {
            Bukkit.getConsoleSender().sendMessage(oldLeaveMessage);
            event.setQuitMessage(null);
            return;
        }

        String leaveMessage = i18n.translate("quit.message");

        event.setQuitMessage(null);

        if (leaveMessage.equalsIgnoreCase("{none}")) {
            return;
        }

        Bukkit.getConsoleSender().sendMessage(oldLeaveMessage);

        leaveMessage = leaveMessage.replace("{name}", event.getPlayer().getName())
                .replace("{displayName}", event.getPlayer().getDisplayName())
                .replace("{prefix}", chat.getGroupPrefix((String) null, chat.getPrimaryGroup(event.getPlayer())));

        leaveMessage = ChatColor.translateAlternateColorCodes('&', leaveMessage);

        Collection<? extends Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());

        Player[] onlinePlayersArray = new Player[onlinePlayers.size()];

        JsonMessage.sendRawJson(leaveMessage, onlinePlayers.toArray(onlinePlayersArray));
    }
}
