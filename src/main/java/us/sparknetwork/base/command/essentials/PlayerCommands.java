package us.sparknetwork.base.command.essentials;

import com.google.inject.Inject;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.LangConfigurations;
import us.sparknetwork.cm.CommandClass;
import us.sparknetwork.cm.annotation.Command;
import us.sparknetwork.cm.command.arguments.CommandContext;

import java.text.MessageFormat;

public class PlayerCommands implements CommandClass {

    @Inject
    private I18n i18n;

    @Command(names = {"fly", "flight"}, max = 1, permission = "base.command.fly", usage = "Usage: /<command> [player]")
    public boolean flyCommand(CommandSender sender, CommandContext args) {
        if (args.getArguments().size() == 0 && !(sender instanceof Player)) {
            return false;
        }

        Player target;

        if (args.getArguments().size() == 0) {
            target = (Player) sender;
        } else {
            if (!sender.hasPermission("base.command.fly.others")) {
                sender.sendMessage(ChatColor.RED + "No Permission.");
                return true;
            }

            target = args.getObject(0, Player.class);

            if (target == null) {
                sender.sendMessage(MessageFormat.format(i18n.translate("offline.player"), args.getArgument(0)));
                return true;
            }
        }

        target.setAllowFlight(!target.getAllowFlight());
        sender.sendMessage(MessageFormat.format(i18n.translate("fly.mode"), target.getDisplayName(), LangConfigurations.convertBoolean(i18n, target.getAllowFlight())));
        return true;
    }

    @Command(names = "feed", max = 1, permission = "base.command.feed", usage = "Usage: /<comand> [player]")
    public boolean feedCommand(CommandSender sender, CommandContext args) {
        if (args.getArguments().size() == 0 && !(sender instanceof Player)) {
            return false;
        }

        Player target;

        if (args.getArguments().size() == 0) {
            target = (Player) sender;
        } else {
            target = args.getObject(0, Player.class);

            if (target == null) {
                sender.sendMessage(MessageFormat.format(i18n.translate("offline.player"), args.getArgument(0)));
                return true;
            }

            if (!sender.hasPermission("base.command.feed.others")) {
                sender.sendMessage(ChatColor.RED + "No permission.");
                return true;
            }

        }

        target.setFoodLevel(20);
        sender.sendMessage(MessageFormat.format(i18n.translate("feeded.player"), target.getDisplayName()));
        return true;
    }

    @Command(names = "heal", max = 1, permission = "base.command.heal", usage = "Usage: /<comand> [player]")
    public boolean healCommand(CommandSender sender, CommandContext args) {
        if (args.getArguments().size() == 0 && !(sender instanceof Player)) {
            return false;
        }

        Player target;

        if (args.getArguments().size() == 0) {
            target = (Player) sender;
        } else {
            target = args.getObject(0, Player.class);

            if (target == null) {
                sender.sendMessage(MessageFormat.format(i18n.translate("offline.player"), args.getArgument(0)));
                return true;
            }

            if (!sender.hasPermission("base.command.heal.others")) {
                sender.sendMessage(ChatColor.RED + "No permission.");
                return true;
            }

        }

        target.setExhaustion(0);
        target.setFireTicks(0);
        target.setHealth(target.getMaxHealth());
        target.setFoodLevel(20);
        sender.sendMessage(MessageFormat.format(i18n.translate("healed.player"), target.getDisplayName()));
        return true;
    }

}
