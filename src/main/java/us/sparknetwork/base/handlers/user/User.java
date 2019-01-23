package us.sparknetwork.base.handlers.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import us.sparknetwork.base.datamanager.Model;

import java.util.List;
import java.util.UUID;

public interface User extends Model {

    @Override
    default String getId() {
        return getIdentity().getUUID().toString();
    }

    Identity getIdentity();

    ConnectionData getConnectionData();

    AddressHistoryData getAddressHistoryData();

    ChatData getChatData();

    ChatSettings getChatSettings();

    WhisperData getWhisperData();

    WhisperSettings getWhisperSettings();

    State getState();


    interface Complete extends User, Identity, ConnectionData, AddressHistoryData, ChatData, ChatSettings, WhisperData, WhisperSettings, State {
        @Override
        default Identity getIdentity() {
            return this;
        }

        @Override
        default ConnectionData getConnectionData() {
            return this;
        }

        @Override
        default AddressHistoryData getAddressHistoryData() {
            return this;
        }

        @Override
        default ChatData getChatData() {
            return this;
        }

        @Override
        default ChatSettings getChatSettings() {
            return this;
        }

        @Override
        default WhisperData getWhisperData() {
            return this;
        }

        @Override
        default WhisperSettings getWhisperSettings() {
            return this;
        }

        @Override
        default State getState() {
            return this;
        }
    }

    interface Identity {
        UUID getUUID();

        @JsonIgnore
        String getLastName();

        List<String> getNameHistory();

        void tryAddName(String name);

        String getNick();
    }

    interface ConnectionData {
        long getLastJoin();

        void setLastJoin(long lastJoin);

        String getLastServerId();

        void setLastServerId(String lastServerId);

        void setOnline(boolean online);

        boolean isOnline();
    }

    interface AddressHistoryData {
        List<String> getAddressHistory();

        @JsonIgnore
        String getLastIp();

        void tryAddAdress(String address);
    }

    interface ChatData {
        long getLastSpeakTime();

        void setLastSpeakTime(long lastSpeakTime);
    }

    interface ChatSettings {
        boolean isGlobalChatVisible();

        void setGlobalChatVisible(boolean globalChatVisible);

        boolean isStaffChatVisible();

        void setStaffChatVisible(boolean staffChatVisible);

        boolean isInStaffChat();

        void setInStaffChat(boolean inStaffChat);
    }

    interface WhisperData {
        UUID getLastPrivateMessageReplier();

        void setLastPrivateMessageReplier(UUID lastPrivateMessageReplier);
    }

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

    interface State {
        boolean isVanished();

        void setVanished(boolean vanished);

        boolean isFreezed();

        void setFreezed(boolean freezed);

        boolean isGodModeEnabled();

        void setGodModeEnabled(boolean godMode);
    }
}
