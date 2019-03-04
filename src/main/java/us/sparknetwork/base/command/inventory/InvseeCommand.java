package us.sparknetwork.base.command.inventory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import us.sparknetwork.base.I18n;
import us.sparknetwork.cm.CommandClass;
import us.sparknetwork.cm.annotation.Command;
import us.sparknetwork.cm.command.arguments.CommandContext;

import java.text.MessageFormat;
import java.util.*;

/**
 * This is a singleton because this command must be created only 1 time, and used 2 times the same instance
 */
@Singleton
public class InvseeCommand implements CommandClass, Listener {

    @Inject
    private I18n i18n;

    private Set<UUID> invseePlayers = new HashSet<>();

    @Command(names = {"invsee", "seeinv", "inventorysee", "seeinventory"}, min = 1, max = 1, permission = "base.command.invsee", onlyPlayer = true, usage = "Usage: /<command> <player>", desc = "This command is used for seeing inventories of another players")
    public boolean invseeCommand(CommandSender commandSender, CommandContext args) {
        Player sender = (Player) commandSender;
        Player target = Bukkit.getPlayer(args.getArgument(0));

        if (target == null) {
            sender.sendMessage(MessageFormat.format(i18n.translate("offline.player"), args.getArgument(0)));
            return true;
        }
        commandSender.sendMessage(MessageFormat.format(i18n.translate("opened.inventory"), target.getDisplayName()));

        sender.closeInventory();
        sender.openInventory(target.getInventory());

        invseePlayers.add(sender.getUniqueId());

        return true;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Inventory playerInventory = e.getPlayer().getInventory();

        if(playerInventory == null){
            return;
        }

        // Workaround for ConcurrentModificationException
        new ArrayList<>(playerInventory.getViewers()).forEach(viewer -> viewer.closeInventory());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent e) {
        HumanEntity whoClicked = e.getWhoClicked();

        if (!(whoClicked instanceof Player)) {
            return;
        }

        Player clicker = (Player) whoClicked;

        Inventory clickedInventory = e.getClickedInventory();

        if (clickedInventory == null || clickedInventory.getType() != InventoryType.PLAYER) {
            return;
        }

        InventoryHolder holder = clickedInventory.getHolder();

        if (holder == null || !(holder instanceof Player)) {
            return;
        }

        Player inventoryHolder = (Player) holder;

        if (!invseePlayers.contains(clicker.getUniqueId())) {
            return;
        }

        if (!clicker.hasPermission("base.invsee.modify")
                || inventoryHolder.hasPermission("base.invsee.preventmodify")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        HumanEntity closer = e.getPlayer();

        if(!(closer instanceof Player)){
            return;
        }

        Player inventoryCloser = (Player) closer;

        invseePlayers.remove(inventoryCloser.getUniqueId());
    }

}
