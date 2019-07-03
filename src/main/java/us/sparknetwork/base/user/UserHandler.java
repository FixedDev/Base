package us.sparknetwork.base.user;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.sparknetwork.base.datamanager.CachedStorageProvider;

public interface UserHandler extends CachedStorageProvider<User.Complete, User.Partial>, Listener {
    @Nullable
    Player getPlayerByNick(@NotNull String nick);
}

