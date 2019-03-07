package us.sparknetwork.base.command.essentials;

import com.google.inject.Inject;
import me.ggamer55.bcm.parametric.CommandClass;
import me.ggamer55.bcm.parametric.annotation.Command;
import me.ggamer55.bcm.parametric.annotation.Optional;
import me.ggamer55.bcm.parametric.annotation.Parameter;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import us.sparknetwork.base.punishment.PunishmentManager;
import us.sparknetwork.base.punishment.PunishmentType;
import us.sparknetwork.base.user.UserHandler;
import us.sparknetwork.utils.DateUtil;
import us.sparknetwork.utils.ListenableFutureUtils;

import java.time.Duration;
import java.time.Instant;

public class PunishmentCommands implements CommandClass {

    @Inject
    private PunishmentManager punishmentManager;
    @Inject
    private UserHandler userHandler;

    @Command(names = {"ban", "tempban"}, permission = "base.command.ban", min = 1, max = 4, usage = "/<command> <target> [duration] [reason] [-s]")
    public boolean banPlayer(CommandSender sender,
                             OfflinePlayer target,
                             @Optional("permanent") String duration,
                             @Optional("none") String reason,
                             @Parameter(value = "silent", isFlag = true) boolean silent) {

        if (!target.isOnline() && sender.hasPermission("base.command.ban.offline")) {
            sender.sendMessage(ChatColor.RED + "No Permission.");

            return true;
        }

        if (target.isOnline() && target.getPlayer().hasPermission("base.command.ban.bypass")) {
            sender.sendMessage(ChatColor.RED + "No Permission.");

            return true;
        }

        Duration banDuration = Duration.ofMillis(DateUtil.parseStringDuration(duration));

        Instant endDate = Instant.now().plus(banDuration);

        ListenableFutureUtils.addCallback(userHandler.findOne(target.getUniqueId().toString()), data -> {
            punishmentManager.createPunishment(PunishmentType.BAN, sender, data, reason,  endDate, false, silent);
        });

        return true;
    }
}
