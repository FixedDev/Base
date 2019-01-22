package us.sparknetwork.base.handlers.user.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
@JsonSerialize(as = UserState.class)
public class BaseUserState implements UserState {

    @Setter(value = AccessLevel.NONE)
    @NonNull
    private UUID uniqueId;

    private boolean vanished;
    @Setter
    private boolean freezed;
    @Setter
    private boolean godModeEnabled;


    public BaseUserState(Player player) {
        this(player.getUniqueId());
    }

    @JsonCreator
    BaseUserState(@JsonProperty("_id") String id,
                  @JsonProperty("vanished") boolean vanished,
                  @JsonProperty("freezed") boolean freezed,
                  @JsonProperty("godModeEnabled") boolean godModeEnabled) {
        this.uniqueId = UUID.fromString(id);
        this.vanished = vanished;
        this.freezed = freezed;
        this.godModeEnabled = godModeEnabled;
    }

    public BaseUserState(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void setVanished(boolean vanished) {
        if(vanished == this.vanished){
            return;
        }

        this.vanished = vanished;

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uniqueId);

        if (!offlinePlayer.isOnline()) {
            return;
        }

        Player player = offlinePlayer.getPlayer();

        UserStateHandler.updateVanishState(player, Bukkit.getOnlinePlayers(), vanished);
    }
}
