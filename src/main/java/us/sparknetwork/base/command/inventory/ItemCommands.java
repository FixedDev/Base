

package us.sparknetwork.base.command.inventory;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.itemdb.ItemDb;
import us.sparknetwork.cm.CommandClass;
import us.sparknetwork.cm.annotation.Command;
import us.sparknetwork.cm.command.arguments.CommandContext;

import java.text.MessageFormat;

import static org.bukkit.command.Command.broadcastCommandMessage;

public class ItemCommands implements CommandClass {

    @Inject
    private I18n i18n;

    @Inject
    private ItemDb itemDb;

    @Command(names = {"give", "giveitem"}, min = 2, max = 3, permission = "base.command.give", usage = "Usage: /<command> <playerName> <itemName> [quantity]", desc = "This command is used for giving items to players")
    public boolean giveCommand(CommandSender sender, CommandContext args) {
        Player target = args.getObject(0, Player.class);

        if (target == null) {
            sender.sendMessage(MessageFormat.format(i18n.translate("offline.player"), args.getArgument(0)));
            return true;
        }

        int itemQuantity = 64;
        if (args.getArguments().size() == 3) {
            itemQuantity = args.getObject(2, Integer.class);
        }

        ItemStack item = itemDb.getItem(args.getArgument(1), itemQuantity);
        if (item == null) {
            sender.sendMessage(MessageFormat.format(i18n.translate("invalid.item"), args.getArgument(1)));
            return true;
        }

        target.getInventory().addItem(item);

        sender.sendMessage(MessageFormat.format(i18n.translate("gived.item"), target.getDisplayName(), item.getType().toString(), Integer.toString(itemQuantity)));

        return true;
    }

    @Command(names = {"item", "i"}, min = 1, max = 2, onlyPlayer = true, permission = "base.command.item", usage = "Usage: /<command> <itemName> [quantity]", desc = "This command gives items to the player sender")
    public boolean itemCommand(Player sender, CommandContext args) {
        int itemQuantity = 64;
        if (args.getArguments().size() == 2) {
            itemQuantity = args.getObject(1, Integer.class);
        }

        ItemStack item = itemDb.getItem(args.getArgument(0), itemQuantity);
        sender.getInventory().addItem(item);

        sender.sendMessage(MessageFormat.format(i18n.translate("gived.item"), sender.getName(), item.getType().toString(), Integer.toString(itemQuantity)));

        return true;
    }

    @Command(names = {"rename"}, min = 1, onlyPlayer = true, permission = "base.command.rename", usage = "Usage: /<command> <itemName>", desc = "This command change the item name in the hand.")
    public boolean renameCommand(Player sender, CommandContext context) {
        ItemStack item = sender.getItemInHand();
        String newName = ChatColor.translateAlternateColorCodes('&', context.getJoinedArgs(0));

        ItemMeta itemMeta = item.getItemMeta();

        if (itemMeta == null) {
            itemMeta = Bukkit.getItemFactory().getItemMeta(item.getType());
        }

        itemMeta.setDisplayName(newName);

        item.setItemMeta(itemMeta);

        sender.sendMessage(MessageFormat.format(i18n.translate("renamed.item"), sender.getName(), item.getType().name(), newName));
        return true;
    }
}
