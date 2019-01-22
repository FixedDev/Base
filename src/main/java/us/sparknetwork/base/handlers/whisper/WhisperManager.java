package us.sparknetwork.base.handlers.whisper;

import com.google.common.util.concurrent.ListenableFuture;
import org.bukkit.entity.Player;
import us.sparknetwork.base.handlers.user.settings.UserSettings;

import java.util.UUID;

public interface WhisperManager {

    ListenableFuture<Void> sendMessageAsync(Player sender, UserSettings from, UUID target, UserSettings to, String content);
}
