package us.sparknetwork.base.handlers.user;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.net.InetAddresses;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.sparknetwork.base.StaffPriority;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonSerialize(as = User.class)
public class BaseUser implements User {

    private BaseIdentity identity;

    private BaseConnectionData connectionData;

    private BaseAddressHistoryData addressHistoryData;

    private BaseChatData chatData;

    private BaseChatSettings chatSettings;

    private BaseWhisperData whisperData;

    private BaseWhisperSettings whisperSettings;

    private BaseState state;

    public BaseUser(UUID uniqueId){
        identity = new BaseIdentity(uniqueId);
        connectionData = new BaseConnectionData();
        addressHistoryData = new BaseAddressHistoryData();
        chatData = new BaseChatData();
        chatSettings = new BaseChatSettings();
        whisperData = new BaseWhisperData();
        whisperSettings = new BaseWhisperSettings();
        state = new BaseState();
    }

    @Override
    public BaseIdentity getIdentity() {
        return identity;
    }

    @Override
    public BaseConnectionData getConnectionData() {
        return connectionData;
    }

    @Override
    public BaseAddressHistoryData getAddressHistoryData() {
        return addressHistoryData;
    }

    @Override
    public BaseChatData getChatData() {
        return chatData;
    }

    @Override
    public BaseChatSettings getChatSettings() {
        return chatSettings;
    }

    @Override
    public BaseWhisperData getWhisperData() {
        return whisperData;
    }

    @Override
    public BaseWhisperSettings getWhisperSettings() {
        return whisperSettings;
    }

    @Override
    public BaseState getState() {
        return state;
    }
    /*
     * Identity implementation
     */

    class BaseIdentity implements Identity {
        @NotNull
        private UUID uuid;
        @NotNull
        private List<String> nameHistory;
        @Nullable
        private String nick;

        BaseIdentity(@NotNull UUID uuid, @NotNull List<String> nameHistory, @Nullable String nick) {
            this.uuid = uuid;
            this.nameHistory = nameHistory;
            this.nick = nick;
        }

        BaseIdentity(@NotNull UUID uniqueId){
            this.uuid = uniqueId;
            this.nameHistory = new ArrayList<>();
            this.nick = null;
        }

        @Override
        @NotNull
        public UUID getUUID() {
            return uuid;
        }

        @Override
        @NotNull
        public List<String> getNameHistory() {
            return new ArrayList<>(nameHistory);
        }

        @Override
        public void tryAddName(@NotNull String name) {
            if (StringUtils.isBlank(name)) throw new IllegalArgumentException("Name is null or empty");
            if (nameHistory.contains(name)) return;
            nameHistory.add(name);
        }

        @Override
        @NotNull
        public String getLastName() {
            Preconditions.checkArgument(!nameHistory.isEmpty(), "No last name found for user " + getId());

            return nameHistory.get(nameHistory.size() - 1);
        }


        @Override
        @Nullable
        public String getNick() {
            return nick;
        }
    }

    /*
     *  ConnectionData implementation
     */

    class BaseConnectionData implements ConnectionData {
        private long lastJoin;
        @Nullable
        private String lastServerId;
        private boolean online = false;

        @Override
        public long getLastJoin() {
            return lastJoin;
        }

        @Override
        public void setLastJoin(long lastJoin) {
            this.lastJoin = lastJoin;
        }

        @Override
        @Nullable
        public String getLastServerId() {
            return lastServerId;
        }

        @Override
        public void setLastServerId(@Nullable String lastServerId) {
            this.lastServerId = lastServerId;
        }

        @Override
        public boolean isOnline() {
            return online;
        }

        @Override
        public void setOnline(boolean online) {
            this.online = online;
        }
    }

    /*
     * AddressHistoryData implementation
     */

    class BaseAddressHistoryData implements AddressHistoryData {

        @NotNull
        private List<String> addressHistory;

        BaseAddressHistoryData(@NotNull List<String> addressHistory) {
            this.addressHistory = addressHistory;
        }

        BaseAddressHistoryData() {
            this.addressHistory = new ArrayList<>();
        }

        @Override
        @NotNull
        public List<String> getAddressHistory() {
            return new ArrayList<>(addressHistory);
        }

        @Override
        public String getLastIp() {
            Preconditions.checkArgument(!addressHistory.isEmpty(), "No last ip found for user " + getId());
            return addressHistory.get(addressHistory.size() - 1);
        }

        @Override
        public void tryAddAdress(@NotNull String address) {
            if (StringUtils.isBlank(address)) throw new IllegalArgumentException("Address is null or empty");
            if (!InetAddresses.isInetAddress(address))
                throw new IllegalArgumentException("Can't add an invalid address");
            if (addressHistory.contains(address)) return;
            addressHistory.add(address);
        }
    }

    /*
     * ChatData implementation
     */

    class BaseChatData implements ChatData {
        private long lastSpeakTime;

        @Override
        public long getLastSpeakTime() {
            return lastSpeakTime;
        }

        @Override
        public void setLastSpeakTime(long lastSpeakTime) {
            this.lastSpeakTime = lastSpeakTime;
        }
    }

    /*
     * ChatSettings implementation
     */

    class BaseChatSettings implements ChatSettings {

        private boolean globalChatVisible = true;
        private boolean staffChatVisible;
        private boolean inStaffChat;

        @Override
        public boolean isGlobalChatVisible() {
            return globalChatVisible;
        }

        @Override
        public void setGlobalChatVisible(boolean globalChatVisible) {
            this.globalChatVisible = globalChatVisible;
        }

        @Override
        public boolean isStaffChatVisible() {
            return staffChatVisible;
        }

        @Override
        public void setStaffChatVisible(boolean staffChatVisible) {
            this.staffChatVisible = staffChatVisible;
        }

        @Override
        public boolean isInStaffChat() {
            return inStaffChat;
        }

        @Override
        public void setInStaffChat(boolean inStaffChat) {
            this.inStaffChat = inStaffChat;
        }
    }

    /*
     * WhisperData implementation
     */

    class BaseWhisperData implements WhisperData {
        @Nullable
        private UUID lastPrivateMessageReplier;

        @Override
        @Nullable
        public UUID getLastPrivateMessageReplier() {
            return lastPrivateMessageReplier;
        }

        public void setLastPrivateMessageReplier(@Nullable UUID lastPrivateMessageReplier) {
            this.lastPrivateMessageReplier = lastPrivateMessageReplier;
        }
    }

    /*
     * WhisperSettings implementation
     */

    class BaseWhisperSettings implements WhisperSettings {

        @NotNull
        private List<UUID> ignoredPlayers;
        private boolean privateMessagesVisible = true;
        private boolean socialSpyVisible;

        BaseWhisperSettings(@NotNull List<UUID> ignoredPlayers, boolean privateMessagesVisible, boolean socialSpyVisible) {
            this.ignoredPlayers = ignoredPlayers;
            this.privateMessagesVisible = privateMessagesVisible;
            this.socialSpyVisible = socialSpyVisible;
        }

        BaseWhisperSettings(@NotNull List<UUID> ignoredPlayers){
            this.ignoredPlayers = ignoredPlayers;
        }

        BaseWhisperSettings(){
            this.ignoredPlayers = new ArrayList<>();
        }

        @Override
        @NotNull
        public List<UUID> getIgnoredPlayers() {
            return new ArrayList<>(ignoredPlayers);
        }

        @Override
        public boolean isPlayerIgnored(UUID playerUUID) {
            return ignoredPlayers.contains(playerUUID);
        }

        @Override
        public void addIgnoredPlayer(UUID playerUUID) {
            if (isPlayerIgnored(playerUUID)) {
                return;
            }

            ignoredPlayers.add(playerUUID);
        }

        @Override
        public void removeIgnoredPlayer(UUID playerUUID) {
            if (!isPlayerIgnored(playerUUID)) {
                return;
            }

            while (isPlayerIgnored(playerUUID)) {
                ignoredPlayers.remove(playerUUID);
            }

        }

        @Override
        public boolean arePrivateMessagesVisible() {
            return privateMessagesVisible;
        }

        @Override
        public void setPrivateMessagesVisible(boolean privateMessagesVisible) {
            this.privateMessagesVisible = privateMessagesVisible;
        }

        @Override
        public boolean isSocialSpyVisible() {
            return socialSpyVisible;
        }

        @Override
        public void setSocialSpyVisible(boolean socialSpyVisible) {
            this.socialSpyVisible = socialSpyVisible;
        }

    }

    /*
     * State implementation
     */

    class BaseState implements State {

        private boolean vanished;
        private boolean freezed;
        private boolean godModeEnabled;

        BaseState(boolean vanished, boolean freezed, boolean godModeEnabled) {
            this.vanished = vanished;
            this.freezed = freezed;
            this.godModeEnabled = godModeEnabled;
        }

        BaseState(){
            this.vanished = false;
            this.freezed = false;
            this.godModeEnabled = false;
        }

        @Override
        public boolean isVanished() {
            return vanished;
        }

        public void setVanished(boolean vanished) {
            if (vanished == this.vanished) {
                return;
            }

            this.vanished = vanished;

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(getIdentity().getUUID());

            if (!offlinePlayer.isOnline()) {
                return;
            }

            Player player = offlinePlayer.getPlayer();

            player.spigot().setCollidesWithEntities(!vanished);
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                if (player.equals(viewer))
                    continue;
                if (vanished) {
                    if (StaffPriority.getByPlayer(player).isMoreThan(StaffPriority.getByPlayer(viewer))) {
                        viewer.hidePlayer(player);
                    }
                    continue;
                }
                viewer.showPlayer(player);
            }
        }

        @Override
        public boolean isFreezed() {
            return freezed;
        }

        @Override
        public void setFreezed(boolean freezed) {
            this.freezed = freezed;
        }

        @Override
        public boolean isGodModeEnabled() {
            return godModeEnabled;
        }

        @Override
        public void setGodModeEnabled(boolean godMode) {
            this.godModeEnabled = godMode;
        }
    }
}
