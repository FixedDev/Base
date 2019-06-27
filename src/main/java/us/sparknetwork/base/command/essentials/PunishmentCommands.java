package us.sparknetwork.base.command.essentials;

import com.google.inject.Inject;
import me.fixeddev.bcm.CommandContext;
import me.fixeddev.bcm.parametric.CommandClass;
import me.fixeddev.bcm.parametric.annotation.Command;
import me.fixeddev.bcm.parametric.annotation.Flag;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.StaffPriority;
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
    public boolean banPlayer(CommandSender sender, CommandContext context, OfflinePlayer target, @Flag('s') boolean silent) {
        return executePunishment(sender, target, context, silent, "ban", PunishmentType.BAN, false);
    }

    @Command(names = {"ipban", "tempipban"}, permission = "base.command.ban", min = 1, usage = "/<command> <target> [duration] [reason] [-s]")
    public boolean ipbanPlayer(CommandSender sender, CommandContext context, OfflinePlayer target, @Flag('s') boolean silent) {
        return executePunishment(sender, target, context, silent, "ipban", PunishmentType.BAN, true);
    }

    @Command(names = {"mute", "tempmute"}, permission = "base.command.mute", min = 1, usage = "/<command> <target> [duration] [reason] [-s]")
    public boolean mutePlayer(CommandSender sender, CommandContext context, OfflinePlayer target, @Flag('s') boolean silent) {
        return executePunishment(sender, target, context, silent, "mute", PunishmentType.MUTE, false);
    }

    @Command(names = "kick", permission = "base.command.kick", min = 1, usage = "/<command> <target> [reason] [-s]")
    public boolean kickPlayer(CommandSender sender, CommandContext context, OfflinePlayer target, @Flag('s') boolean silent) {
        if (!target.isOnline()) {
            sender.sendMessage(i18n.format("offline.player", target.getName()));

            return true;
        }

        checkPriorityOrSendNoPermission(sender, target);

        if (context.getArgumentsLength() == 1) {
            // Only the target is provided, create a permanent kick if the sender has permission
            if (!sender.hasPermission("base.command.kick.permanent")) {
                sender.sendMessage(NO_PERMISSION_MESSAGE);
                return true;
            }

            ListenableFutureUtils.addCallback(userHandler.findOne(target.getUniqueId().toString()), data ->
                    punishmentManager.createPunishment(PunishmentType.KICK, sender, data, i18n.translate("punishment.default.reason"), null, false, silent)
            );

            return true;
        }

        String reason = context.getJoinedArgs(1);

        ListenableFutureUtils.addCallback(userHandler.findOne(target.getUniqueId().toString()), data ->
                punishmentManager.createPunishment(PunishmentType.KICK, sender, data, reason, null, false, silent)
        );

        return true;
    }

    @Command(names = "unban", min = 1, max = 1, permission = "base.command.unban")
    public boolean unbanPlayer(CommandSender sender, OfflinePlayer target) {
        String ipAddress = null;

        if (target.isOnline()) {
            ipAddress = target.getPlayer().getAddress().getAddress().getHostAddress();
        }

        ListenableFutureUtils.addCallback(punishmentManager.getLastPunishment(PunishmentType.BAN, target.getUniqueId(), ipAddress), object -> {
            if (object == null) {
                sender.sendMessage(i18n.format("punishment.user.not.banned", target.getName()));
                return;
            }

            object.setActive(false);
            punishmentManager.savePunishment(object);

            sender.sendMessage(i18n.format("punishment.unbanned", target.getName()));
        });
        return true;
    }

    @Command(names = "unmute", min = 1, max = 1, permission = "base.command.unmute")
    public boolean unmutePlayer(CommandSender sender, OfflinePlayer target) {
        ListenableFutureUtils.addCallback(punishmentManager.getLastPunishment(PunishmentType.MUTE, target.getUniqueId(), null), object -> {
            if (object == null) {
                sender.sendMessage(i18n.format("punishment.user.not.muted", target.getName()));
                return;
            }

            object.setActive(false);
            punishmentManager.savePunishment(object);

            sender.sendMessage(i18n.format("punishment.unmuted", target.getName()));
        });
        return true;
    }


    private boolean executePunishment(CommandSender sender, OfflinePlayer target, CommandContext context, boolean silent, String identifier, PunishmentType type, boolean ipPunishment) {
        if (!target.isOnline() && !sender.hasPermission("base.command." + identifier + ".offline")) {
            sender.sendMessage(NO_PERMISSION_MESSAGE);

            return true;
        }

        if (!checkPriorityOrSendNoPermission(sender, target)) {
            return true;
        }

        ListenableFutureUtils.addCallback(punishmentManager.getLastPunishment(type, target.getUniqueId(), null), oldPunishment -> {
            if (oldPunishment != null) {
                long secondsLeft = 0;

                if (!oldPunishment.isPermanent() && oldPunishment.getEndDate() != null) {
                    secondsLeft = ZonedDateTime.now().until(oldPunishment.getEndDate(), ChronoUnit.SECONDS);
                }

                if ((oldPunishment.isPermanent() || secondsLeft >= 0) && !sender.hasPermission("base.command." + identifier + ".override")) {
                    sender.sendMessage(NO_PERMISSION_MESSAGE);

                    return;
                }

                oldPunishment.setActive(false);
                punishmentManager.savePunishment(oldPunishment);
            }
        });

        if (context.getArgumentsLength() == 1) {
            // Only the target is provided, create a permanent punishment if the sender has permission
            if (!sender.hasPermission("base.command." + identifier + ".permanent")) {
                sender.sendMessage(NO_PERMISSION_MESSAGE);
                return true;
            }

            ListenableFutureUtils.addCallback(userHandler.findOne(target.getUniqueId().toString()), data -> {
                String punishedIp = null;

                if (target.isOnline()) {
                    punishedIp = target.getPlayer().getAddress().getAddress().getHostAddress();
                }

                punishmentManager.createPunishment(type, sender, target.getUniqueId(), target.getName(), punishedIp, i18n.translate("punishment.default.reason"), null, ipPunishment, silent);
            });

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
            // It is, so execute all the code for temporal punishments
            if (!sender.hasPermission("base.command." + identifier + ".temporal")) {
                sender.sendMessage(NO_PERMISSION_MESSAGE);

                return true;
            }

            String reason = i18n.translate("punishment.default.reason");

            if (context.getArgumentsLength() > 2) {
                reason = context.getJoinedArgs(2);
            }

            final String punishmentReason = reason;

            Duration punishmentDuration = Duration.ofMillis(durationInMillis);

            final ZonedDateTime punishmentEndDate = ZonedDateTime.now().plus(punishmentDuration);

            ListenableFutureUtils.addCallback(userHandler.findOne(target.getUniqueId().toString()), data -> {
                String punishedIp = null;

                if (target.isOnline()) {
                    punishedIp = target.getPlayer().getAddress().getAddress().getHostAddress();
                }

                punishmentManager.createPunishment(type, sender, target.getUniqueId(), target.getName(), punishedIp, punishmentReason, punishmentEndDate, ipPunishment, silent);
            });

            return true;
        }

        // The target is provided, a duration is also provided, but is invalid, it will be included as part of the reason

        // This is again checking if the sender has permission for permanent punishments
        if (!sender.hasPermission("base.command." + identifier + ".permanent")) {
            sender.sendMessage(NO_PERMISSION_MESSAGE);
            return true;
        }

        String reason = context.getJoinedArgs(1);

        ListenableFutureUtils.addCallback(userHandler.findOne(target.getUniqueId().toString()), data -> {
            String punishedIp = null;

            if (target.isOnline()) {
                punishedIp = target.getPlayer().getAddress().getAddress().getHostAddress();
            }

            punishmentManager.createPunishment(type, sender, target.getUniqueId(), target.getName(), punishedIp, reason, null, ipPunishment, silent);
        });

        return true;
    }

    /**
     * @param sender - The sender of the command
     * @param target - The target to check against
     * @return - If the sender has more priority than the target
     */
    private boolean checkPriorityOrSendNoPermission(CommandSender sender, OfflinePlayer target) {
        StaffPriority senderPriority = StaffPriority.getByCommandSender(sender);
        StaffPriority targetPriority = StaffPriority.NONE;

        if (target.isOnline()) {
            targetPriority = StaffPriority.getByCommandSender(target.getPlayer());
        }

        if (targetPriority.isMoreThan(senderPriority)) {
            sender.sendMessage(NO_PERMISSION_MESSAGE);

            return false;
        }

        return true;
    }
}
