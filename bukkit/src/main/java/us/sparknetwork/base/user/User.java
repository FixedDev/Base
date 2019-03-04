package us.sparknetwork.base.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.jetbrains.annotations.NotNull;
import us.sparknetwork.base.api.datamanager.Model;

import java.util.List;
import java.util.UUID;

@JsonDeserialize(as = BaseUser.class)
public interface User extends Model, Identity {

    @Override
    default String getId() {
        return getUUID().toString();
    }

    interface Complete extends User, ConnectionData, AddressHistoryData, ChatData, ChatSettings, WhisperData, WhisperSettings, Friends, State {
    }

    interface ConnectionData extends Identity {
        long getLastJoin();

        void setLastJoin(long lastJoin);

        String getLastServerId();

        void setLastServerId(String lastServerId);

        void setOnline(boolean online);

        boolean isOnline();
    }

    interface AddressHistoryData extends Identity {
        List<String> getAddressHistory();

        @JsonIgnore
        String getLastIp();

        void tryAddAdress(String address);
    }

    interface ChatData extends Identity {
        long getLastSpeakTime();

        void setLastSpeakTime(long lastSpeakTime);
    }

    interface ChatSettings extends Identity {
        boolean isGlobalChatVisible();

        void setGlobalChatVisible(boolean globalChatVisible);

        boolean isStaffChatVisible();

        void setStaffChatVisible(boolean staffChatVisible);

        boolean isInStaffChat();

        void setInStaffChat(boolean inStaffChat);
    }

    interface WhisperData extends Identity {
        UUID getLastPrivateMessageReplier();

        void setLastPrivateMessageReplier(UUID lastPrivateMessageReplier);
    }

    interface WhisperSettings extends Identity {
        List<UUID> getIgnoredPlayers();

        boolean isPlayerIgnored(UUID playerUUID);

        void addIgnoredPlayer(UUID playerUUID);

        void removeIgnoredPlayer(UUID playerUUID);

        @NotNull
        WhisperVisibility getPrivateMessagesVisibility();

        void setPrivateMessagesVisibility(@NotNull WhisperVisibility visibility);

        boolean isSocialSpyVisible();

        void setSocialSpyVisible(boolean socialSpyVisible);
    }

    enum WhisperVisibility {
        NONE, FRIENDS, ALL
    }

    interface Friends extends Identity {
        @NotNull
        List<UUID> getFriends();

        void addFriend(@NotNull Identity identity);

        void removeFriend(@NotNull Identity identity);

        @JsonIgnore
        int getFriendsNumber();

        @JsonIgnore
        boolean isFriendOf(@NotNull Identity identity);

        int getFriendsLimit();

        void setFriendsLimit(int limit);
    }

    interface State extends Identity {
        boolean isVanished();

        void setVanished(boolean vanished);

        boolean isFreezed();

        void setFreezed(boolean freezed);

        boolean isGodModeEnabled();

        void setGodModeEnabled(boolean godMode);
    }
}
