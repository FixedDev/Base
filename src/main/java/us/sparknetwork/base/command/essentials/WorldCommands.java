package us.sparknetwork.base.command.essentials;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.sparknetwork.base.I18n;
import us.sparknetwork.cm.CommandClass;
import us.sparknetwork.cm.annotation.Command;
import us.sparknetwork.cm.command.arguments.CommandContext;

public class WorldCommands implements CommandClass {

    @Inject
    private I18n i18n;
/*
    @Inject
    @Named("worlds")
    private Config worldsConfig;
*/
    @Command(names = {"world", "worldtp", "tptoworld"}, permission = "base.command.world", usage = "Usage: /<command> <world> [targetPlayer]", min = 1, max = 2)
    public boolean worldTp(CommandSender commandSender, CommandContext context) {
        Player target;

        if (context.getArguments().size() == 1 && commandSender instanceof Player) {
            target = (Player) commandSender;
        } else if (context.getArguments().size() == 2) {
            target = context.getObject(1, Player.class);

            if (target == null) {
                commandSender.sendMessage(i18n.format("offline.player", context.getArgument(1)));
                return true;
            }
        } else {
            return false;
        }

        World world = Bukkit.getWorld(context.getArgument(0));

        if (world == null) {
            commandSender.sendMessage(i18n.format("world.invalid.name", context.getArgument(0)));
            return true;
        }

        target.teleport(world.getSpawnLocation());

        String targetName = i18n.translate("yourself");

        if (commandSender != target) {
            targetName = target.getDisplayName();
        }

        commandSender.sendMessage(i18n.format("teleported.to", targetName,
                i18n.format("teleported.to.world",
                        world.getName(),
                        world.getEnvironment().toString()
                )));

        return true;
    }
}
