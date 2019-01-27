package us.sparknetwork.base.command.tell;

import com.google.inject.Inject;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import us.sparknetwork.base.I18n;

import us.sparknetwork.base.handlers.user.User;
import us.sparknetwork.base.handlers.user.UserHandler;
import us.sparknetwork.base.handlers.user.finder.UserFinder;
import us.sparknetwork.base.handlers.whisper.WhisperManager;
import us.sparknetwork.cm.CommandClass;
import us.sparknetwork.cm.annotation.Command;
import us.sparknetwork.cm.command.arguments.CommandContext;
import us.sparknetwork.utils.ListenableFutureUtils;

import static us.sparknetwork.utils.ListenableFutureUtils.*;

public class SendCommands implements CommandClass {
    @Inject
    private I18n i18n;
    @Inject
    private UserHandler dataHandler;
    @Inject
    private UserFinder userFinder;
    @Inject
    private WhisperManager whisperManager;

    @Command(
            names = {"tell", "msg", "whisper", "w", "m", "pm", "privatemessage", "message"},
            min = 2,
            onlyPlayer = true,
            permission = "base.command.tell",
            usage = "Usage: /<command> <target> <message...>"
    )
    public boolean tellCommand(Player sender, CommandContext context) {
        OfflinePlayer target = context.getObject(0, OfflinePlayer.class);

        if (target.getUniqueId() == null) {
            sender.sendMessage(this.i18n.format("user.not.found", context.getArgument(0)));
            return true;
        }

        addCallback(ListenableFutureUtils.combine(this.dataHandler.findOne(sender.getUniqueId().toString()), this.dataHandler.findOne(target.getUniqueId().toString())), (data) -> {
            User.Complete from = data.getFirst();
            User.Complete to = data.getSecond();

            if (to == null) {
                sender.sendMessage(this.i18n.format("user.not.found", context.getArgument(0)));
                return;
            }

            this.whisperManager.sendMessageAsync(sender, from, to, context.getJoinedArgs(1));
        });
        return true;
    }

    @Command(
            names = {"reply", "r", "tellreply", "whisperreply"},
            min = 1,
            onlyPlayer = true,
            permission = "base.command.tell",
            usage = "Usage: /<command> <message...>"
    )
    public boolean replyCommand(Player sender, CommandContext context) {
        addCallback(this.dataHandler.findOne(sender.getUniqueId().toString()), (userData) -> {
            if (userData.getLastPrivateMessageReplier() == null) {
                sender.sendMessage(this.i18n.translate("reply.target.unavailable"));
                return;
            }

            User.Complete targetData = this.dataHandler.findOneSync(userData.getLastPrivateMessageReplier().toString());

            if (targetData == null) {
                sender.sendMessage(this.i18n.format("user.not.found", context.getArgument(0)));
                return;
            }

            this.whisperManager.sendMessageAsync(sender, userData, targetData, context.getJoinedArgs(0));
        });
        return true;
    }
}
