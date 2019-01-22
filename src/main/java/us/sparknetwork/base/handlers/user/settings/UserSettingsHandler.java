package us.sparknetwork.base.handlers.user.settings;

import org.bukkit.entity.Player;
import us.sparknetwork.base.datamanager.CachedStorageProvider;

import javax.annotation.Nullable;

public interface UserSettingsHandler extends CachedStorageProvider<UserSettings> {
    @Nullable
    Player getPlayerByNick(String nickname);
}
