package us.sparknetwork.base.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
import us.sparknetwork.base.event.UserNickChangeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonSerialize(as = User.Complete.class)
@JsonIgnoreProperties("privateMessagesVisible")
public class BaseUser implements User.Complete {

    /*
     * Identity fields
     */

    @NotNull
    private UUID uuid;
    @NotNull
    private List<String> nameHistory;
    @Nullable
    private String nick;

    /*
     * ConnectionData Fields
     */

    private long lastJoin;
    @Nullable
    private String lastServerId;
    private boolean online = false;

    /*
     * AddressHistoryData fields
     */

    @NotNull
    private List<String> addressHistory;

    /*
     * ChatData fields
     */

    private long lastSpeakTime;

    /*
     * ChatSettings fields
     */

    private boolean globalChatVisible;
    private boolean staffChatVisible;
    private boolean inStaffChat;

    /*
     * WhisperData fields
     */

    @Nullable
    private UUID lastPrivateMessageReplier;

    /*
     * WhisperSettings fields
     */

    @NotNull
    private List<UUID> ignoredPlayers;
    @NotNull
    private WhisperVisibility visibility;
    private boolean socialSpyVisible;

    /*
     * Friends fields
     */
    @NotNull
    private List<UUID> friends;
    private int friendsLimit;

    /*
     * State fields
     */

    private boolean vanished;
    private boolean freezed;
    private boolean godModeEnabled;

    @JsonCreator
    public BaseUser(@NotNull @JsonProperty("_id") String id,
                    @NotNull @JsonProperty("nameHistory") List<String> nameHistory,
                    @Nullable @JsonProperty("nick") String nick,
                    @JsonProperty("lastJoin") long lastJoin,
                    @Nullable @JsonProperty("lastServerId") String lastServerId,
                    @JsonProperty("online") boolean online,
                    @NotNull @JsonProperty("addressHistory") List<String> addressHistory,
                    @JsonProperty("lastSpeakTime") long lastSpeakTime,
                    @JsonProperty("globalChatVisible") boolean globalChatVisible,
                    @JsonProperty("staffChatVisible") boolean staffChatVisible,
                    @JsonProperty("inStaffChat") boolean inStaffChat,
                    @Nullable @JsonProperty("lastPrivateMessageReplier") UUID lastPrivateMessageReplier,
                    @NotNull @JsonProperty("ignoredPlayers") List<UUID> ignoredPlayers,
                    @JsonProperty(value = "privateMessagesVisibility") WhisperVisibility visibility,
                    @JsonProperty("socialSpyVisible") boolean socialSpyVisible,
                    @JsonProperty("friends") @NotNull List<UUID> friends,
                    @JsonProperty("friendsLimit") int friendsLimit,
                    @JsonProperty("vanished") boolean vanished,
                    @JsonProperty("freezed") boolean freezed,
                    @JsonProperty("godModeEnabled") boolean godModeEnabled) {
        this.uuid = UUID.fromString(id);
        this.nameHistory = nameHistory;
        this.nick = nick;
        this.lastJoin = lastJoin;
        this.lastServerId = lastServerId;
        this.online = online;
        this.addressHistory = addressHistory;
        this.lastSpeakTime = lastSpeakTime;
        this.globalChatVisible = globalChatVisible;
        this.staffChatVisible = staffChatVisible;
        this.inStaffChat = inStaffChat;
        this.lastPrivateMessageReplier = lastPrivateMessageReplier;
        this.ignoredPlayers = ignoredPlayers;
        this.visibility = visibility;
        this.socialSpyVisible = socialSpyVisible;
        this.friends = friends;
        this.friendsLimit = friendsLimit;
        this.vanished = vanished;
        this.freezed = freezed;
        this.godModeEnabled = godModeEnabled;

        // I don't care if intellij says that it can't be null, jackson can actually make it null
        if (friends == null) {
            this.friends = new ArrayList<>();
        }
    }

    public BaseUser(@NotNull UUID uuid) {
        this.uuid = uuid;
        this.nameHistory = new ArrayList<>();
        this.addressHistory = new ArrayList<>();
        this.ignoredPlayers = new ArrayList<>();
        this.friends = new ArrayList<>();
        this.globalChatVisible = true;
        this.visibility = WhisperVisibility.NONE;
    }


    /*
     * Identity implementation
     */
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
    @Nullable
    public String getNick() {
        return nick;
    }

    public void setNick(@Nullable String nick) {
        this.nick = nick;

        Bukkit.getPluginManager().callEvent(new UserNickChangeEvent(this, this.nick, nick));
    }


    @Override
    @NotNull
    public String getLastName() {
        if(nameHistory.isEmpty()){
            throw new IllegalStateException("No last name found for user" + getUUID());
        }

        return nameHistory.get(nameHistory.size() - 1);
    }

    /*
     *  ConnectionData implementation
     */

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

    /*
     * AddressHistoryData implementation
     */

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

    /*
     * ChatData implementation
     */

    @Override
    public long getLastSpeakTime() {
        return lastSpeakTime;
    }

    @Override
    public void setLastSpeakTime(long lastSpeakTime) {
        this.lastSpeakTime = lastSpeakTime;
    }

    /*
     * ChatSettings implementation
     */

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

    /*
     * WhisperData implementation
     */

    @Override
    @Nullable
    public UUID getLastPrivateMessageReplier() {
        return lastPrivateMessageReplier;
    }

    public void setLastPrivateMessageReplier(@Nullable UUID lastPrivateMessageReplier) {
        this.lastPrivateMessageReplier = lastPrivateMessageReplier;
    }

    /*
     * WhisperSettings implementation
     */

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

    @NotNull
    @Override
    public WhisperVisibility getPrivateMessagesVisibility() {
        return visibility;
    }

    @Override
    public void setPrivateMessagesVisibility(@NotNull WhisperVisibility visibility) {
        this.visibility = visibility;
    }


    @Override
    public boolean isSocialSpyVisible() {
        return socialSpyVisible;
    }

    @Override
    public void setSocialSpyVisible(boolean socialSpyVisible) {
        this.socialSpyVisible = socialSpyVisible;
    }

    /*
     * State implementation
     */

    @Override
    public boolean isVanished() {
        return vanished;
    }

    public void setVanished(boolean vanished) {
        this.vanished = vanished;

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(getUUID());

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

    /*
     * Friends Implementation
     */
    @Override
    public List<UUID> getFriends() {
        return new ArrayList<>(friends);
    }

    @Override
    public void addFriend(@NotNull Identity userId) {
        if (friends.contains(userId.getUUID())) {
            return;
        }

        friends.add(userId.getUUID());
    }

    @Override
    public void removeFriend(@NotNull Identity userId) {
        if (!friends.contains(userId.getUUID())) {
            return;
        }

        friends.remove(userId.getUUID());
    }

    @Override
    public boolean isFriendOf(@NotNull Identity identity) {
        return friends.contains(identity.getUUID());
    }

    @Override
    public int getFriendsLimit() {
        return friendsLimit;
    }

    @Override
    public void setFriendsLimit(int limit) {
        this.friendsLimit = limit;
    }
}
