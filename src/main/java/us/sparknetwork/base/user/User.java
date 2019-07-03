package us.sparknetwork.base.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.sparknetwork.base.datamanager.Model;
import us.sparknetwork.base.datamanager.PartialModel;

import java.util.List;
import java.util.UUID;

@JsonDeserialize(as = BaseUser.class)
public interface User extends Model {

    interface Partial extends PartialModel {}

    interface Complete extends User, Identity, ConnectionData, AddressHistoryData, ChatData, ChatSettings, WhisperData, WhisperSettings, Friends, State {
    }

    interface Identity extends Partial {
        @JsonIgnore
        @NotNull
        UUID getUUID();

        @JsonIgnore
        @NotNull
        String getLastName();

        @NotNull
        List<String> getNameHistory();

        void tryAddName(String var1);

        @Nullable
        String getNick();

        default boolean hasNick() {
            return this.getNick() != null;
        }

        void setNick(@Nullable String var1);
    }


    interface ConnectionData extends Partial {
        long getLastJoin();

        void setLastJoin(long lastJoin);

        String getLastServerId();

        void setLastServerId(String lastServerId);

        void setOnline(boolean online);

        boolean isOnline();
    }

    interface AddressHistoryData extends Partial {
        List<String> getAddressHistory();

        @JsonIgnore
        String getLastIp();

        void tryAddAdress(String address);
    }

    interface ChatData extends Partial {
        long getLastSpeakTime();

        void setLastSpeakTime(long lastSpeakTime);
    }

    interface ChatSettings extends Partial {
        boolean isGlobalChatVisible();

        void setGlobalChatVisible(boolean globalChatVisible);

        boolean isStaffChatVisible();

        void setStaffChatVisible(boolean staffChatVisible);

        boolean isInStaffChat();

        void setInStaffChat(boolean inStaffChat);
    }

    interface WhisperData extends Partial {
        UUID getLastPrivateMessageReplier();

        void setLastPrivateMessageReplier(UUID lastPrivateMessageReplier);
    }

    interface WhisperSettings extends Partial {
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

    interface Friends extends Partial {
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

    interface State extends Partial {
        boolean isVanished();

        void setVanished(boolean vanished);

        boolean isFreezed();

        void setFreezed(boolean freezed);

        boolean isGodModeEnabled();

        void setGodModeEnabled(boolean godMode);
    }
}
