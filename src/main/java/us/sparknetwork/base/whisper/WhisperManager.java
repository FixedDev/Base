package us.sparknetwork.base.whisper;

import com.google.common.util.concurrent.ListenableFuture;
import org.bukkit.entity.Player;
import us.sparknetwork.base.user.User;


public interface WhisperManager {
    ListenableFuture<Void> sendMessageAsync(Player sender, User.Complete from, User.Complete to, String content);
}
