package us.sparknetwork.base.command.essentials;

import com.google.inject.Inject;
import me.ggamer55.bcm.CommandContext;
import me.ggamer55.bcm.parametric.CommandClass;
import me.ggamer55.bcm.parametric.annotation.Command;
import me.ggamer55.bcm.parametric.annotation.Parameter;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.punishment.PunishmentManager;
import us.sparknetwork.base.punishment.PunishmentType;
import us.sparknetwork.base.user.UserHandler;
import us.sparknetwork.utils.DateUtil;
import us.sparknetwork.utils.ListenableFutureUtils;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class PunishmentCommands implements CommandClass {

    @Inject
    private PunishmentManager punishmentManager;
    @Inject
    private UserHandler userHandler;
    @Inject
    private I18n i18n;

    private static final String NO_PERMISSION_MESSAGE = ChatColor.RED + "No permission.";

    @Command(names = {"ban", "tempban"}, permission = "base.command.ban", min = 1, usage = "/<command> <target> [duration] [reason] [-s]")
    public boolean banPlayer(CommandSender sender, CommandContext context, OfflinePlayer target, @Parameter(value = "s", isFlag = true) boolean silent) {

        if (!target.isOnline() && !sender.hasPermission("base.command.ban.offline")) {
            sender.sendMessage(NO_PERMISSION_MESSAGE);

            return true;
        }

        if (target.isOnline() && target.getPlayer().hasPermission("base.command.ban.bypass")) {
            sender.sendMessage(NO_PERMISSION_MESSAGE);

            return true;
        }

        ListenableFutureUtils.addCallback(punishmentManager.getPunishment(PunishmentType.BAN, target.getUniqueId(), null), oldPunishment -> {
            if (oldPunishment != null) {
                long secondsLeft = 0;

                if (!oldPunishment.isPermanent() && oldPunishment.getEndDate() != null) {
                    secondsLeft = ZonedDateTime.now().until(oldPunishment.getEndDate(), ChronoUnit.SECONDS);
                }

                if ((oldPunishment.isPermanent() || secondsLeft >= 0) && !sender.hasPermission("base.command.ban.override")) {
                    sender.sendMessage(NO_PERMISSION_MESSAGE);

                    return;
                }

                oldPunishment.setActive(false);
                punishmentManager.savePunishment(oldPunishment);
            }
        });

        if (context.getArgumentsLength() == 1) {
            // Only the target is provided, create a permanent ban if the sender has permission
            if (!sender.hasPermission("base.command.ban.permanent")) {
                sender.sendMessage(NO_PERMISSION_MESSAGE);
                return true;
            }

            ListenableFutureUtils.addCallback(userHandler.findOne(target.getUniqueId().toString()), data ->
                    punishmentManager.createPunishment(PunishmentType.BAN, sender, data, i18n.translate("punishment.default.reason"), null, false, silent)
            );

            return true;
        }

        // We have more than 1 args, so, the first argument provided should be the target, the second the duration, and the extras should be the reason
        String stringDuration = context.getArgument(1);
        long durationInMillis;

        // Checking if the duration is valid
        try {
            // Well is valid
            durationInMillis = DateUtil.parseStringDuration(stringDuration);
        } catch (NumberFormatException ex) {
            // Is invalid, also we should use a different number than 0
            durationInMillis = 0L;
        }

        // Checking if the duration is more than 0 millis
        if (durationInMillis > 0) {
            // It is, so execute all the code for temporal bans
            if (!sender.hasPermission("base.command.ban.temporal")) {
                sender.sendMessage(NO_PERMISSION_MESSAGE);

                return true;
            }

            String reason = i18n.translate("punishment.default.reason");

            if (context.getArgumentsLength() > 2) {
                reason = context.getJoinedArgs(2);
            }

            final String banReason = reason;

            Duration banDuration = Duration.ofMillis(durationInMillis);

            final ZonedDateTime banEndDate = ZonedDateTime.now().plus(banDuration);

            ListenableFutureUtils.addCallback(userHandler.findOne(target.getUniqueId().toString()), data ->
                    punishmentManager.createPunishment(PunishmentType.BAN, sender, data, banReason, banEndDate, false, silent)
            );

            return true;
        }

        // The target is provided, a duration is also provided, but is invalid, it will be included as part of the reason

        // This is again checking if the sender has permission for permanent bans
        if (!sender.hasPermission("base.command.ban.permanent")) {
            sender.sendMessage(NO_PERMISSION_MESSAGE);
            return true;
        }

        String reason = context.getJoinedArgs(1);

        ListenableFutureUtils.addCallback(userHandler.findOne(target.getUniqueId().toString()), data ->
                punishmentManager.createPunishment(PunishmentType.BAN, sender, data, reason, null, false, silent)
        );

        return true;
    }
}
