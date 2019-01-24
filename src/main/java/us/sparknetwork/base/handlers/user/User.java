package us.sparknetwork.base.handlers.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import us.sparknetwork.base.datamanager.Model;

import java.util.List;
import java.util.UUID;

@JsonDeserialize(as = BaseUser.class)
public interface User extends Model, Identity {

    @Override
    default String getId() {
        return getUUID().toString();
    }

    interface Complete extends User, ConnectionData, AddressHistoryData, ChatData, ChatSettings, WhisperData, WhisperSettings, State {
    }

    @JsonDeserialize(as = BaseUser.BaseConnectionData.class)
    interface ConnectionData {
        long getLastJoin();

        void setLastJoin(long lastJoin);

        String getLastServerId();

        void setLastServerId(String lastServerId);

        void setOnline(boolean online);

        boolean isOnline();
    }

    @JsonDeserialize(as = BaseUser.BaseAddressHistoryData.class)
    interface AddressHistoryData {
        List<String> getAddressHistory();

        @JsonIgnore
        String getLastIp();

        void tryAddAdress(String address);
    }

    @JsonDeserialize(as = BaseUser.BaseChatData.class)
    interface ChatData {
        long getLastSpeakTime();

        void setLastSpeakTime(long lastSpeakTime);
    }

    @JsonDeserialize(as = BaseUser.BaseChatSettings.class)
    interface ChatSettings {
        boolean isGlobalChatVisible();

        void setGlobalChatVisible(boolean globalChatVisible);

        boolean isStaffChatVisible();

        void setStaffChatVisible(boolean staffChatVisible);

        boolean isInStaffChat();

        void setInStaffChat(boolean inStaffChat);
    }

    @JsonDeserialize(as = BaseUser.BaseWhisperData.class)
    interface WhisperData {
        UUID getLastPrivateMessageReplier();

        void setLastPrivateMessageReplier(UUID lastPrivateMessageReplier);
    }

    @JsonDeserialize(as = BaseUser.BaseWhisperSettings.class)
    interface WhisperSettings {
        List<UUID> getIgnoredPlayers();

        boolean isPlayerIgnored(UUID playerUUID);

        void addIgnoredPlayer(UUID playerUUID);

        void removeIgnoredPlayer(UUID playerUUID);

        boolean arePrivateMessagesVisible();

        void setPrivateMessagesVisible(boolean privateMessagesVisible);

        boolean isSocialSpyVisible();

        void setSocialSpyVisible(boolean socialSpyVisible);
    }

    @JsonDeserialize(as = BaseUser.BaseState.class)
    interface State {
        boolean isVanished();

        void setVanished(boolean vanished);

        boolean isFreezed();

        void setFreezed(boolean freezed);

        boolean isGodModeEnabled();

        void setGodModeEnabled(boolean godMode);
    }
}
