package us.sparknetwork.base.handlers.user.settings;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import us.sparknetwork.base.datamanager.Model;

import java.util.List;
import java.util.UUID;

@JsonDeserialize(as = BaseUserSettings.class)
public interface UserSettings extends Model {

    @Override
    default String getId() {
        return getUniqueId().toString();
    }

    UUID getUniqueId();

    String getNickname();

    boolean isGlobalChatVisible();

    boolean isStaffChatVisible();

    boolean isInStaffChat();

    boolean isPrivateMessagesVisible();

    boolean isSocialSpyVisible();

    List<UUID> getIgnoredPlayers();

    boolean isPlayerIgnored(UUID playerUUID);

    void setNickname(String nickname);

    void setGlobalChatVisible(boolean globalChatVisible);

    void setStaffChatVisible(boolean staffChatVisible);

    void setInStaffChat(boolean inStaffChat);

    void setPrivateMessagesVisible(boolean privateMessagesVisible);

    void setSocialSpyVisible(boolean socialSpyVisible);

    void addIgnoredPlayer(UUID playerUUID);

    void removeIgnoredPlayer(UUID playerUUID);
}
