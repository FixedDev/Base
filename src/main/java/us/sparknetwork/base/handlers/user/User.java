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

        boolean getPrivateMessagesVisible();

        void setPrivateMessagesVisible(boolean privateMessagesVisible);

        boolean isSocialSpyVisible();

        void setSocialSpyVisible(boolean socialSpyVisible);
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
