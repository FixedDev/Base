package us.sparknetwork.base.handlers.user.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import us.sparknetwork.base.datamanager.Model;

import java.util.List;
import java.util.UUID;

@JsonDeserialize(as = BaseUserData.class)
public interface UserData extends Model {

    @Override
    default String getId(){
        return getUniqueId().toString();
    }

    UUID getUniqueId();

    List<String> getNameHistory();

    List<String> getAddressHistory();

    @JsonIgnore
    String getLastName();

    @JsonIgnore
    String getLastIp();

    long getLastSpeakTime();

    long getLastJoin();

    String getLastServerId();

    void tryAddAdress(String address);

    void tryAddName(String name);

    UUID getLastPrivateMessageReplier();

    boolean isOnline();

    void setLastSpeakTime(long lastSpeakTime);

    void setLastJoin(long lastJoin);

    void setLastServerId(String lastServerId);

    void setLastPrivateMessageReplier(UUID lastPrivateMessageReplier);

    void setOnline(boolean online);
}
