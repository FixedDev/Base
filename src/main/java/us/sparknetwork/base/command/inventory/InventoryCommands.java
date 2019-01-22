

package us.sparknetwork.base.command.inventory;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.sparknetwork.base.I18n;
import us.sparknetwork.cm.CommandClass;
import us.sparknetwork.cm.annotation.Command;
import us.sparknetwork.cm.command.arguments.CommandContext;

import java.text.MessageFormat;

public class InventoryCommands implements CommandClass {

    @Inject
    private I18n i18n;

    @Command(names = {"invsee", "seeinv", "inventorysee", "seeinventory"}, min = 1, max = 1, permission = "base.command.invsee", onlyPlayer = true, usage = "Usage: /<command> <player>", desc = "This command is used for seeing inventories of another players")
    public boolean invseeCommand(CommandSender commandSender, CommandContext args) {
        Player sender = (Player) commandSender;
        Player target = Bukkit.getPlayer(args.getArgument(0));
        if (target == null) {
            sender.sendMessage(MessageFormat.format(i18n.translate("offline.player"), args.getArgument(0)));
            return true;
        }
        commandSender.sendMessage(MessageFormat.format(i18n.translate("opened.inventory"), target.getDisplayName()));
        
        sender.openInventory(target.getInventory());
        return true;
    }

    @Command(names = {"copyinv", "invcopy", "inventorycopy", "copyinventory"}, min = 1, max = 1, permission = "base.command.copyinventory", onlyPlayer = true, usage = "Usage: /<command> <player>", desc = "This command is used for copy inventories of another players to your inventory")
    public boolean copyInventoryCommand(Player commandSender, CommandContext args) {
        Player sender = commandSender;
        Player target = Bukkit.getPlayer(args.getArgument(0));
        if (target == null) {
            sender.sendMessage(MessageFormat.format(i18n.translate("offline.player"), args.getArgument(0)));
            return true;
        }
        commandSender.sendMessage(MessageFormat.format(i18n.translate("copied.inventory"), target.getDisplayName()));

        sender.getInventory().setArmorContents(target.getInventory().getArmorContents());
        sender.getInventory().setContents(target.getInventory().getContents());
        return true;
    }

    @Command(names = {"clearinv", "invclear", "inventoryclear", "clearinventory", "ci"}, max = 1, permission = "base.command.clearinventory", usage = "Usage: /<command> [player]", desc = "This command is used for clear your inventory or inventories of another players")
    public boolean clearInventoryCommand(CommandSender commandSender, CommandContext args) {
        Player target;

        if (!(commandSender instanceof Player)) {
            if (args.getArguments().size() < 1) return false;
            target = Bukkit.getPlayer(args.getArgument(0));
            if (target == null) {
                commandSender.sendMessage(i18n.format("offline.player", args.getArgument(0)));
                return true;
            }

        } else {
            if (!commandSender.hasPermission("base.command.clearinventory.others")) {
                commandSender.sendMessage(ChatColor.RED + "No permission.");
                return true;
            }

            target = (Player) commandSender;
            if (args.getArguments().size() == 1) {
                target = Bukkit.getPlayer(args.getArgument(0));

                if (target == null) {
                    commandSender.sendMessage(i18n.format("offline.player", args.getArgument(0)));
                    return true;
                }

            }
        }

        commandSender.sendMessage(i18n.format("cleared.inventory", target == commandSender ? i18n.translate("your") : target.getDisplayName()));

        target.getInventory().clear();
        target.getInventory().setArmorContents(new ItemStack[4]);
        return true;
    }
}
