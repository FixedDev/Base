package us.sparknetwork.base.command.essentials;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import us.sparknetwork.base.I18n;
import us.sparknetwork.cm.CommandClass;
import us.sparknetwork.cm.annotation.Command;
import us.sparknetwork.cm.command.arguments.CommandContext;

import java.text.MessageFormat;

public class TeleportCommands implements CommandClass {

    @Inject
    private I18n i18n;

    @Command(names = {"teleport", "tp"}, permission = "base.command.teleport", min = 1, max = 4, usage = "Usage: /<command> [targetPlayer] <destinationPlayer> \n /<command> [targetPlayer] <x> <y> <z>", desc = "This command is used for teleporting")
    public boolean teleportCommand(CommandSender commandSender, CommandContext args) {

        if (args.getArguments().size() == 1 && (commandSender instanceof Player)) {
            Player sender = (Player) commandSender;
            Player destinationPlayer = args.getObject(0, Player.class);

            if (destinationPlayer == null) {
                sender.sendMessage(MessageFormat.format(i18n.translate("offline.player"), args.getArgument(0)));
                return true;
            }
            sender.teleport(destinationPlayer, PlayerTeleportEvent.TeleportCause.COMMAND);

            commandSender.sendMessage(MessageFormat.format(i18n.translate("teleported.to"), sender.getDisplayName(), destinationPlayer.getDisplayName()));
            return true;
        }

        if (args.getArguments().size() == 2) {
            Player targetPlayer = args.getObject(0, Player.class);
            Player destinationPlayer = args.getObject(1, Player.class);

            if (targetPlayer == null || destinationPlayer == null) {
                commandSender.sendMessage(i18n.translate("offline.player"));
                return true;
            }
            targetPlayer.teleport(destinationPlayer, PlayerTeleportEvent.TeleportCause.COMMAND);

            commandSender.sendMessage(MessageFormat.format(i18n.translate("teleported.to"), targetPlayer.getDisplayName(), destinationPlayer.getDisplayName()));
            return true;
        }
        if (args.getArguments().size() == 3 && (commandSender instanceof Player)) {
            double x = args.getObject(0, Double.class);
            double y = args.getObject(1, Double.class);
            double z = args.getObject(2, Double.class);

            Player sender = (Player) commandSender;

            Location locToTeleport = new Location(sender.getWorld(), x, y, z);
            locToTeleport.getChunk();

            sender.teleport(locToTeleport);

            commandSender.sendMessage(MessageFormat.format(i18n.translate("teleported.to"), sender.getDisplayName(), x + " " + y + " " + z));
            return true;
        }

        if (args.getArguments().size() == 4) {
            Player target = args.getObject(0, Player.class);

            if (target == null) {
                commandSender.sendMessage(MessageFormat.format(i18n.translate("offline.player"), args.getArgument(0)));
                return true;
            }

            double x = args.getObject(1, Double.class);
            double y = args.getObject(2, Double.class);
            double z = args.getObject(3, Double.class);

            Location locToTeleport = new Location(target.getWorld(), x, y, z);
            locToTeleport.getChunk();

            target.teleport(locToTeleport);

            commandSender.sendMessage(MessageFormat.format(i18n.translate("teleported.to"), target.getDisplayName(), x + " " + y + " " + z));
            return true;
        }

        return false;
    }

    @Command(names = {"teleporthere", "tphere", "tph", "s"}, permission = "base.command.teleporthere", min = 1, max = 1, onlyPlayer = true, usage = "Usage: /<command> <playerName>", desc = "This command is used for teleporting another player to you")
    public boolean teleportHereCommand(Player commandSender, CommandContext args) {
        Player target = args.getObject(0, Player.class);

        if (target == null) {
            commandSender.sendMessage(MessageFormat.format(i18n.translate("offline.player"), args.getArgument(0)));
            return true;
        }

        target.teleport(commandSender);

        commandSender.sendMessage(MessageFormat.format(i18n.translate("teleported.to"), target.getDisplayName(), commandSender.getDisplayName()));
        return true;
    }

    @Command(names = {"top"}, permission = "base.command.top", min = 0, max = 0, onlyPlayer = true, usage = "Usage: /<command>", desc = "This command is used for teleporting to the highest spot in your location")
    public boolean topCommand(Player sender, CommandContext args) {
        Location highestLocation = sender.getWorld().getHighestBlockAt(sender.getLocation()).getLocation();
        highestLocation.add(0, 0.5, 0);
        sender.teleport(highestLocation);

        sender.sendMessage(i18n.format("teleported.to", sender.getDisplayName(), i18n.translate("teleported.to.highest.location")));

        return true;
    }

    @Command(names = {"teleportall", "tpall"}, permission = "base.command.teleportall", min = 0, max = 4, usage = "Usage: /<command> [target]\n /<command> <x> <y> <z> <world> ", desc = "This command is used for teleporting all the players to you or a specified target")
    public boolean teleportAllCommand(CommandSender sender, CommandContext context) {
        if (context.getArguments().size() == 0 && !(sender instanceof Player)) {
            return false;
        }

        if (context.getArguments().size() == 0) {
            Player teleportTarget = (Player) sender;

            if (teleportTarget == null) {
                sender.sendMessage(MessageFormat.format(i18n.translate("offline.player"), context.getArgument(0)));
                return true;
            }

            Bukkit.getOnlinePlayers().forEach(player -> player.teleport(teleportTarget));

            sender.sendMessage(MessageFormat.format(i18n.translate("teleported.to"), "all the players", teleportTarget.getDisplayName()));
            return true;
        }
        if (context.getArguments().size() == 3 || context.getArguments().size() == 4) {
            double x = context.getObject(0, Double.class);
            double y = context.getObject(1, Double.class);
            double z = context.getObject(2, Double.class);


            World world = Bukkit.getWorld(context.getArgument(3));

            if (world == null) {
                return false;
            }

            Location location = new Location(world, x, y, z);

            Bukkit.getOnlinePlayers().forEach(player -> player.teleport(location));

            sender.sendMessage(i18n.format("teleported.to", "all the players", String.join(", ", world.getName(), x + "", y + "", z + "")));
            return true;
        }

        return false;
    }
}
