package us.sparknetwork.base.listeners;

import com.google.inject.Inject;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.PlaceholderApiReplacer;
import us.sparknetwork.base.ServerConfigurations;
import us.sparknetwork.base.chat.ChatFormat;
import us.sparknetwork.base.chat.ChatFormatManager;
import us.sparknetwork.base.user.User;
import us.sparknetwork.base.user.UserHandler;
import us.sparknetwork.base.listeners.message.StaffChatListener;
import us.sparknetwork.base.messager.Channel;
import us.sparknetwork.base.messager.Messenger;
import us.sparknetwork.base.messager.messages.StaffChatMessage;
import us.sparknetwork.utils.DateUtil;
import us.sparknetwork.utils.JsonMessage;

import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ChatListener implements Listener {

    private Channel<StaffChatMessage> staffChatChannel;

    @Inject
    private UserHandler settingsHandler;

    @Inject
    private ChatFormatManager chatFormatManager;

    @Inject
    private I18n i18n;

    @Inject
    private Chat chat;

    @Inject
    public ChatListener(Messenger messager, StaffChatListener listener) {
        staffChatChannel = messager.getChannel("staffChat", StaffChatMessage.class);

        staffChatChannel.registerListener(listener);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void chatListener(AsyncPlayerChatEvent e) {
        Optional<User.Complete> optionalSettings = Optional.ofNullable(settingsHandler.findOneSync(e.getPlayer().getUniqueId().toString()));

        if(e.isCancelled()){
            return; // Just in case
        }

        if (!optionalSettings.isPresent()) {
            e.getPlayer().sendMessage(i18n.translate("load.fail.settings"));

            e.setCancelled(true);
            return;
        }

        User.Complete userSettings = optionalSettings.get();

        if (userSettings.isInStaffChat()) {
            handleStaffChat(e, userSettings);
        } else {
            handleNormalChat(e, userSettings);
        }
    }

    private void handleNormalChat(AsyncPlayerChatEvent e, User.Complete userData) {
        Set<String> userIds = e.getRecipients().stream().map(Player::getUniqueId).map(UUID::toString).collect(Collectors.toSet());

        Set<User.Complete> userSettingsSet = settingsHandler.findSync(userIds, userIds.size());

        Set<Player> chatRecipients = userSettingsSet.stream().filter(User.ChatSettings::isGlobalChatVisible).map(settings -> Bukkit.getPlayer(settings.getUUID())).collect(Collectors.toSet());

        e.getRecipients().clear();
        e.getRecipients().addAll(chatRecipients);

        long timeBeforeUnmute = ServerConfigurations.MUTED_CHAT - System.currentTimeMillis();

        if (timeBeforeUnmute > 0 && !e.getPlayer().hasPermission("base.chat.muted.bypass")) {
            e.setCancelled(true);

            e.getPlayer().sendMessage(MessageFormat.format(i18n.translate("already.muted.chat"), DateUtil.getHumanReadableDate(timeBeforeUnmute, i18n)));
            return;
        }

        long timeBeforeTalk = (ServerConfigurations.SLOW_CHAT_DELAY * 1000 + userData.getLastSpeakTime()) - System.currentTimeMillis();
        if (ServerConfigurations.SLOW_CHAT >= System.currentTimeMillis() && timeBeforeTalk > 0 && !e.getPlayer().hasPermission("base.chat.slow.bypass")) {
            e.setCancelled(true);

            e.getPlayer().sendMessage(MessageFormat.format(i18n.translate("already.slowed.chat"), DateUtil.getHumanReadableDate(timeBeforeTalk, i18n)));
            return;
        }

        if (i18n.translate("chat.format").equalsIgnoreCase("{format}")) {
            return;
        }

        if (e.getPlayer().hasPermission("base.chat.color")) {
            e.setMessage(ChatColor.translateAlternateColorCodes('&', e.getMessage()));
        } else {
            e.setMessage(e.getMessage().replaceAll("&[A-Fa-f0-9[lkmno]]", ""));
        }

        e.setCancelled(true);

        ChatFormat playerChatFormat = chatFormatManager.getChatFormatForPlayer(e.getPlayer());

        if (playerChatFormat.isUsePlaceholderApi() && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            Bukkit.getLogger().log(Level.WARNING, "There's a chat format with PlaceholderAPI usage enabled, but PlaceholderAPI is not installed, disabling PlaceholderAPI use.");

            playerChatFormat.setUsePlaceholderApi(false);
        }

        e.setMessage(ChatColor.translateAlternateColorCodes('&', playerChatFormat.getChatColor()) + e.getMessage());

        Bukkit.getConsoleSender().sendMessage(String.format(e.getFormat(), e.getPlayer().getName(), e.getMessage()));

        String chatFormat = playerChatFormat.constructJsonMessage().append(e.getMessage()).save().toString();

        chatFormat = chatFormat
                .replace("{displayName}", e.getPlayer().getDisplayName())
                .replace("{name}", e.getPlayer().getName())
                .replace("{world}", e.getPlayer().getWorld().getName())
                .replace("{prefix}", this.getPrefix(e.getPlayer()))
                .replace("{suffix}", this.getSuffix(e.getPlayer()));

        chatFormat = ChatColor.translateAlternateColorCodes('&', chatFormat);

        Player[] messageRecipientsArray = e.getRecipients().toArray(new Player[0]);

        if (playerChatFormat.isUsePlaceholderApi()) {
            chatFormat = PlaceholderApiReplacer.parsePlaceholders(e.getPlayer(), chatFormat);

            if (playerChatFormat.isAllowRelationalPlaceholders()) {
                for (Player viewer : messageRecipientsArray) {
                    chatFormat = PlaceholderApiReplacer.parseRelationalPlaceholders(e.getPlayer(), viewer, chatFormat);

                    JsonMessage.sendRawJson(chatFormat, viewer);
                }

                return;
            }
        }

        JsonMessage.sendRawJson(chatFormat, messageRecipientsArray);
    }

    private void handleStaffChat(AsyncPlayerChatEvent e, User.Complete userSettings) {
        if (!userSettings.isStaffChatVisible()) {
            e.getPlayer().sendMessage(i18n.translate("staff.chat.invisible"));
        }

        if (e.getPlayer().hasPermission("base.staffchat.color")) {
            e.setMessage(ChatColor.translateAlternateColorCodes('&', e.getMessage()));
        }

        Set<String> userIds = Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("base.command.staffchat.see")).map(Player::getUniqueId).map(UUID::toString).collect(Collectors.toSet());

        Set<Player> players = settingsHandler.findSync(userIds, userIds.size()).stream().filter(Objects::nonNull).filter(User.ChatSettings::isStaffChatVisible).map(playerSettings -> Bukkit.getPlayer(playerSettings.getUUID())).collect(Collectors.toSet());

        e.getRecipients().clear();
        e.getRecipients().addAll(players);

        e.setFormat(i18n.format("chat.staff.format", e.getPlayer().getName(), "%2$s"));

        StaffChatMessage message = new StaffChatMessage(e.getPlayer().getName(), e.getMessage());

        staffChatChannel.sendMessage(message);
    }

    private String getPrefix(Player player) {
        String prefix = chat.getGroupPrefix((String) null, chat.getPrimaryGroup(player));
        return ChatColor.translateAlternateColorCodes('&', prefix);
        /*
        StringBuilder joiner = new StringBuilder();
        String[] playerGroups = plugin.getChat().getPlayerGroups(player);
        for (String group : playerGroups) {
            String prefix = plugin.getChat().getGroupPrefix((String) null, group);
            if (StringUtils.isNotBlank(prefix)) joiner.append(prefix);
        }
        return ChatColor.translateAlternateColorCodes('&', joiner.toString());*/
    }

    private String getSuffix(Player player) {
        String prefix = chat.getGroupSuffix((String) null, chat.getPrimaryGroup(player));
        return ChatColor.translateAlternateColorCodes('&', prefix);

        /*StringBuilder joiner = new StringBuilder();
        String[] playerGroups = plugin.getChat().getPlayerGroups(player);
        for (String group : playerGroups) {
            String suffix = plugin.getChat().getGroupSuffix((String) null, group);
            if (StringUtils.isNotBlank(suffix)) joiner.append(suffix);
        }
        return ChatColor.translateAlternateColorCodes('&', joiner.toString());*/
    }
}
