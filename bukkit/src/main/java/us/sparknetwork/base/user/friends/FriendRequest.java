package us.sparknetwork.base.user.friends;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import us.sparknetwork.base.datamanager.Model;

import java.time.Instant;
import java.util.UUID;

@Data
public class FriendRequest implements Model {
    @NotNull
    private final String id;

    @NotNull
    private final UUID from;
    @NotNull
    private final UUID to;

    @NotNull
    private Instant requestTime;

    @JsonCreator
    FriendRequest(@NotNull @JsonProperty("_id") String id,
                  @NotNull @JsonProperty("from") UUID from,
                  @NotNull @JsonProperty("to") UUID to,
                  @NotNull @JsonProperty("requestTime") Instant requestTime) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.requestTime = requestTime;
    }

    public FriendRequest(@NotNull UUID from, @NotNull UUID to) {
        this.id = UUID.randomUUID().toString();
        this.from = from;
        this.to = to;
        requestTime = Instant.now();
    }

    @Override
    public String getId() {
        return to.toString();
    }
}