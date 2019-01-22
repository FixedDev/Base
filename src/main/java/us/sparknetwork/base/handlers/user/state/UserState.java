package us.sparknetwork.base.handlers.user.state;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import us.sparknetwork.base.datamanager.Model;

import java.util.UUID;

@JsonDeserialize(as = BaseUserState.class)
public interface UserState extends Model {
    @Override
    default String getId() {
        return getUniqueId().toString();
    }

    UUID getUniqueId();

    boolean isVanished();

    boolean isFreezed();

    boolean isGodModeEnabled();

    void setVanished(boolean vanished);

    void setFreezed(boolean freezed);

    void setGodModeEnabled(boolean godMode);
}
