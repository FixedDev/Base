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
import us.sparknetwork.base.ServerConfigurations;
import us.sparknetwork.base.handlers.user.data.UserData;
import us.sparknetwork.base.handlers.user.data.UserDataHandler;
import us.sparknetwork.base.handlers.user.settings.UserSettings;
import us.sparknetwork.base.handlers.user.settings.UserSettingsHandler;
import us.sparknetwork.base.handlers.user.state.UserState;
import us.sparknetwork.base.listeners.message.StaffChatListener;
import us.sparknetwork.base.messager.Channel;
import us.sparknetwork.base.messager.Messenger;
import us.sparknetwork.base.messager.messages.StaffChatMessage;
import us.sparknetwork.utils.DateUtil;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static us.sparknetwork.utils.ListenableFutureUtils.addCallback;
import static us.sparknetwork.utils.ListenableFutureUtils.addOptionalToReturnValue;

public class ChatListener implements Listener {

    private Channel<StaffChatMessage> staffChatChannel;

    @Inject
    private UserSettingsHandler settingsHandler;
    @Inject
    private UserDataHandler dataHandler;

    @Inject
    private I18n i18n;

    @Inject
    private Chat chat;

    @Inject
    public ChatListener(Messenger messager, StaffChatListener listener) {
        staffChatChannel = messager.getChannel("staffChat", StaffChatMessage.class);

        staffChatChannel.registerListener(listener);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void chatListener(AsyncPlayerChatEvent e) {
        Optional<UserSettings> optionalSettings = Optional.ofNullable(settingsHandler.findOneSync(e.getPlayer().getUniqueId().toString()));

        if (!optionalSettings.isPresent()) {
            e.getPlayer().sendMessage(i18n.translate("load.fail.settings"));

            e.setCancelled(true);
            return;
        }
        UserSettings userSettings = optionalSettings.get();

        Optional<UserData> optionalUserData = Optional.ofNullable(dataHandler.findOneSync(e.getPlayer().getUniqueId().toString()));

        if (!optionalUserData.isPresent()) {
            e.getPlayer().sendMessage(i18n.translate("load.fail.data"));

            e.setCancelled(true);
            return;
        }

        UserData userData = optionalUserData.get();

        if (userSettings.isInStaffChat()) {
            handleStaffChat(e, userSettings);
        } else {
            handleNormalChat(e, userData);
        }
    }

    private void handleNormalChat(AsyncPlayerChatEvent e, UserData userData) {
        Set<String> userIds = Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).map(UUID::toString).collect(Collectors.toSet());

        Set<UserSettings> userSettingsSet = settingsHandler.findSync(userIds, userIds.size());

        Set<Player> chatRecipients = userSettingsSet.stream().filter(UserSettings::isGlobalChatVisible).map(settings -> Bukkit.getPlayer(settings.getUniqueId())).collect(Collectors.toSet());

        e.getRecipients().clear();
        e.getRecipients().addAll(chatRecipients);

        long timeBeforeUnmute = ServerConfigurations.MUTED_CHAT - System.currentTimeMillis();

        if (timeBeforeUnmute > 0 && !e.getPlayer().hasPermission("base.chat.muted.bypass")) {
            e.setCancelled(true);

            e.getPlayer().sendMessage(MessageFormat.format(i18n.translate("already.muted.chat"), DateUtil.getHumanReadableDate(timeBeforeUnmute)));
            return;
        }

        long timeBeforeTalk = (ServerConfigurations.SLOW_CHAT_DELAY * 1000 + userData.getLastSpeakTime()) - System.currentTimeMillis();
        if (ServerConfigurations.SLOW_CHAT >= System.currentTimeMillis() && timeBeforeTalk > 0 && !e.getPlayer().hasPermission("base.chat.slow.bypass")) {
            e.setCancelled(true);

            e.getPlayer().sendMessage(MessageFormat.format(i18n.translate("already.slowed.chat"), DateUtil.getHumanReadableDate(timeBeforeTalk)));
            return;
        }

        if (i18n.translate("chat.format").equalsIgnoreCase("{format}")) {
            return;
        }

        if (e.getPlayer().hasPermission("base.chat.color")) {
            e.setMessage(ChatColor.translateAlternateColorCodes('&', e.getMessage()));
        }

        String format = i18n.translate("chat.format")
                .replace("{displayName}", "%1$s")
                .replace("{name}", e.getPlayer().getName())
                .replace("{world}", e.getPlayer().getWorld().getName())
                .replace("{chat}", "%2$s")
                .replace("{prefix}", this.getPrefix(e.getPlayer()))
                .replace("{suffix}", this.getSuffix(e.getPlayer()));
        e.setFormat(format);
    }

    private void handleStaffChat(AsyncPlayerChatEvent e, UserSettings userSettings) {
        e.setCancelled(true);

        if (!userSettings.isStaffChatVisible()) {
            e.getPlayer().sendMessage(i18n.translate("staff.chat.invisible"));
        }

        if (e.getPlayer().hasPermission("base.staffchat.color")) {
            e.setMessage(ChatColor.translateAlternateColorCodes('&', e.getMessage()));
        }

        String staffFormattedMessage = MessageFormat.format(i18n.translate("chat.staff.format"), e.getPlayer().getName(), e.getMessage());

        Bukkit.getConsoleSender().sendMessage(staffFormattedMessage);

        Set<String> userIds = Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("base.command.staffchat.see")).map(Player::getUniqueId).map(UUID::toString).collect(Collectors.toSet());

        addCallback(settingsHandler.find(userIds, userIds.size()), userSettingsSet -> {
            userSettingsSet.stream().filter(Objects::nonNull).filter(UserSettings::isStaffChatVisible).map(playerSettings -> Bukkit.getPlayer(playerSettings.getUniqueId())).forEach(player -> player.sendMessage(staffFormattedMessage));
        });

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
