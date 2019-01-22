package us.sparknetwork.base.handlers.user.settings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.sparknetwork.base.handlers.user.state.BaseUserState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@JsonSerialize(as = UserSettings.class)
public class BaseUserSettings implements UserSettings {

    @Setter(value = AccessLevel.NONE)
    @NonNull
    @JsonIgnore
    private UUID uniqueId;

    private String nickname;

    private boolean globalChatVisible = true;
    private boolean staffChatVisible;
    private boolean inStaffChat;

    private boolean privateMessagesVisible = true;

    private boolean socialSpyVisible;

    @Setter(value = AccessLevel.NONE)
    @NonNull
    private List<UUID> ignoredPlayers;

    @JsonCreator
    BaseUserSettings(@JsonProperty("_id") String id,
                     @JsonProperty("nickname") String nickname,
                     @JsonProperty("globalChatVisible") boolean globalChatVisible,
                     @JsonProperty("staffChatVisible") boolean staffChatVisible,
                     @JsonProperty("inStaffChat") boolean inStaffChat,
                     @JsonProperty("privateMessagesVisible") boolean privateMessagesVisible,
                     @JsonProperty("socialSpyVisible") boolean socialSpyVisible,
                     @JsonProperty("ignoredPlayers") List<UUID> ignoredPlayers) {
        this.uniqueId = UUID.fromString(id);
        this.nickname = nickname;
        this.globalChatVisible = globalChatVisible;
        this.staffChatVisible = staffChatVisible;
        this.inStaffChat = inStaffChat;
        this.privateMessagesVisible = privateMessagesVisible;
        this.socialSpyVisible = socialSpyVisible;
        this.ignoredPlayers = ignoredPlayers;
    }

    public BaseUserSettings(UUID userId) {
        this(userId, new ArrayList<>());
    }

    public BaseUserSettings(Player player) {
        this(player.getUniqueId());
    }

    public BaseUserSettings(UUID uniqueId, List<UUID> ignoredPlayers) {
        this.uniqueId = uniqueId;
        this.ignoredPlayers = ignoredPlayers;
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

}
