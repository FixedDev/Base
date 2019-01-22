package us.sparknetwork.base.handlers.user.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.net.InetAddresses;
import lombok.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.util.*;

@Data
@JsonSerialize(as = UserData.class)
public class BaseUserData implements UserData {

    @Setter(value = AccessLevel.NONE)
    @NonNull
    @JsonIgnore
    private UUID uniqueId;

    @Setter(value = AccessLevel.PRIVATE)
    @NonNull
    private List<String> nameHistory;

    @Setter(value = AccessLevel.PRIVATE)
    @NonNull
    private List<String> addressHistory;

    private long lastSpeakTime;
    private long lastJoin;
    private String lastServerId;
    private UUID lastPrivateMessageReplier;

    private boolean online = false;

    @JsonCreator
    BaseUserData(@JsonProperty("_id") String id,
                 @JsonProperty("nameHistory") List<String> nameHistory,
                 @JsonProperty("addressHistory") List<String> addressHistory,
                 @JsonProperty("lastSpeakTime") long lastSpeakTime,
                 @JsonProperty("lastJoin") long lastJoin,
                 @JsonProperty("lastServerId") String lastServerId,
                 @JsonProperty("lastPrivateMessageReplier") UUID lastPrivateMessageReplier,
                 @JsonProperty("online") boolean online) {
        this.uniqueId = UUID.fromString(id);
        this.nameHistory = nameHistory;
        this.addressHistory = addressHistory;
        this.lastSpeakTime = lastSpeakTime;
        this.lastJoin = lastJoin;
        this.lastServerId = lastServerId;
        this.lastPrivateMessageReplier = lastPrivateMessageReplier;
        this.online = online;
    }

    public BaseUserData(UUID uniqueId) {
        this(uniqueId, new ArrayList<>(), new ArrayList<>());
    }

    public BaseUserData(Player player) {
        this(player.getUniqueId(), Collections.singletonList(player.getName()), Collections.singletonList(player.getAddress().getAddress().getHostAddress()));
    }

    public BaseUserData(UUID uniqueId, List<String> nameHistory, List<String> addressHistory) {
        this.uniqueId = uniqueId;
        this.nameHistory = new LinkedList<>(nameHistory);
        this.addressHistory = new LinkedList<>(addressHistory);
    }

    @Override
    public void tryAddAdress(String address) {
        if (StringUtils.isBlank(address)) throw new IllegalArgumentException("Address is null or empty");
        if (!InetAddresses.isInetAddress(address)) throw new IllegalArgumentException("Can't add an invalid address");
        if (addressHistory.contains(address)) return;
        addressHistory.add(address);
    }

    @Override
    public void tryAddName(String name) {
        if (StringUtils.isBlank(name)) throw new IllegalArgumentException("Name is null or empty");
        if (nameHistory.contains(name)) return;
        nameHistory.add(name);
    }

    @Override
    public String getLastName() {
        Preconditions.checkArgument(!nameHistory.isEmpty(), "No last name found for user " + uniqueId);

        return nameHistory.get(nameHistory.size() - 1);
    }

    @Override
    public String getLastIp() {
        Preconditions.checkArgument(!addressHistory.isEmpty(), "No last ip found for user " + uniqueId);
        return addressHistory.get(addressHistory.size() - 1);
    }
}
