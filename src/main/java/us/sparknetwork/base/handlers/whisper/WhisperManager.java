package us.sparknetwork.base.handlers.whisper;

import com.google.common.util.concurrent.ListenableFuture;
import org.bukkit.entity.Player;
import us.sparknetwork.base.handlers.user.User;


public interface WhisperManager {
    ListenableFuture sendMessageAsync(Player sender, User.Complete from, User.Complete to, String content);
}
