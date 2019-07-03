package us.sparknetwork.base.user.session;

import org.jetbrains.annotations.NotNull;
import us.sparknetwork.base.datamanager.Model;

import java.util.UUID;

public interface Session extends Model {
    @NotNull
    UUID playerId();

    @NotNull
    String serverId();
}
