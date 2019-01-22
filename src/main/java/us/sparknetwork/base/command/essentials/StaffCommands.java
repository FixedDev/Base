

package us.sparknetwork.base.command.essentials;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.LangConfigurations;
import us.sparknetwork.base.handlers.user.state.BaseUserState;
import us.sparknetwork.base.handlers.user.state.UserState;
import us.sparknetwork.base.handlers.user.state.UserStateHandler;
import us.sparknetwork.cm.CommandClass;
import us.sparknetwork.cm.annotation.Command;
import us.sparknetwork.cm.command.arguments.CommandContext;

import java.text.MessageFormat;

import static us.sparknetwork.utils.ListenableFutureUtils.addCallback;
import static us.sparknetwork.utils.ListenableFutureUtils.addOptionalToReturnValue;

public class StaffCommands implements CommandClass {

    @Inject
    private I18n i18n;
    @Inject
    private UserStateHandler stateHandler;
    @Inject
    private JavaPlugin plugin;

    @Command(names = {"god", "godmode"}, max = 1, permission = "base.command.god", usage = "Usage: /<command> [player]")
    public boolean godCommand(CommandSender sender, CommandContext args) {
        if (args.getArguments().size() == 0 && !(sender instanceof Player)) {
            return false;
        }

        Player target;

        if (args.getArguments().size() == 0) {
            target = (Player) sender;
        } else {
            if (!sender.hasPermission("base.command.god.others")) {
                sender.sendMessage(ChatColor.RED + "No Permission.");
                return true;
            }

            target = args.getObject(0, Player.class);

            if (target == null) {
                sender.sendMessage(MessageFormat.format(i18n.translate("offline.player"), args.getArgument(0)));
                return true;
            }
        }

        addCallback(addOptionalToReturnValue(stateHandler.findOne(target.getUniqueId().toString())), optionalState -> {
            UserState state = optionalState.orElse(new BaseUserState(target.getUniqueId()));

            String bool = LangConfigurations.convertBoolean(i18n, !state.isGodModeEnabled());

            state.setGodModeEnabled(!state.isGodModeEnabled());
            stateHandler.save(state);

            sender.sendMessage(MessageFormat.format(i18n.translate("god.mode"), target.getDisplayName(), bool));
        });


        return true;
    }

    @Command(names = {"freeze"}, min = 1, max = 1, permission = "base.command.freeze", usage = "Usage: /<command> [player]")
    public boolean freezeCommand(CommandSender sender, CommandContext args) {
        Player target = args.getObject(0, Player.class);

        addCallback(addOptionalToReturnValue(stateHandler.findOne(target.getUniqueId().toString())), optionalState -> {
            UserState state = optionalState.orElse(new BaseUserState(target.getUniqueId()));

            if (target == null) {
                sender.sendMessage(MessageFormat.format(i18n.translate("offline.player"), args.getArgument(0)));
                return;
            }

            if (state.isFreezed()) {
                sender.sendMessage(MessageFormat.format(i18n.translate("player.already.freezed"), target.getDisplayName()));
                return;
            }

            state.setFreezed(true);
            stateHandler.save(state);

            sender.sendMessage(MessageFormat.format(i18n.translate("unfreezed.player"), target.getName(), i18n.translate("true")));
        });

        return true;
    }

    @Command(names = {"unfreeze"}, min = 1, max = 1, permission = "base.command.freeze", usage = "Usage: /<command> [player]")
    public boolean unfreezeCommand(CommandSender sender, CommandContext args) {
        OfflinePlayer target = args.getObject(0, OfflinePlayer.class);

        addCallback(addOptionalToReturnValue(stateHandler.findOne(target.getUniqueId().toString())), optionalState -> {
            UserState state = optionalState.orElse(new BaseUserState( target.getUniqueId()));

            if (!target.isOnline() && !state.isFreezed()) {
                sender.sendMessage(MessageFormat.format(i18n.translate("offline.player"), args.getArgument(0)));
                return;
            }

            if (!state.isFreezed()) {
                sender.sendMessage(MessageFormat.format(i18n.translate("player.not.freezed"), target.getName()));
                return;
            }

            state.setFreezed(false);
            stateHandler.save(state);

            sender.sendMessage(MessageFormat.format(i18n.translate("unfreezed.player"), target.getName(), i18n.translate("false")));
        });

        return true;
    }

    @Command(names = {"vanish", "v"}, max = 1, permission = "base.command.vanish", usage = "Usage: /<command> [player]")
    public boolean vanishCommand(CommandSender sender, CommandContext args) {
        if (args.getArguments().size() == 0 && !(sender instanceof Player)) {
            return false;
        }

        Player target;

        if (args.getArguments().size() == 0) {
            target = (Player) sender;
        } else {
            if (!sender.hasPermission("base.command.vanish.others")) {
                sender.sendMessage(ChatColor.RED + "No Permission.");
                return true;
            }

            target = args.getObject(0, Player.class);

            if (target == null) {
                sender.sendMessage(MessageFormat.format(i18n.translate("offline.player"), args.getArgument(0)));
                return true;
            }
        }
        addCallback(addOptionalToReturnValue(stateHandler.findOne(target.getUniqueId().toString())), optionalState -> {
            UserState state = optionalState.orElse(new BaseUserState(target.getUniqueId()));

            Bukkit.getScheduler().runTask(plugin, () ->{
                state.setVanished(!state.isVanished());
                stateHandler.save(state);
            });


            String bool = LangConfigurations.convertBoolean(i18n, state.isVanished());
            sender.sendMessage(MessageFormat.format(i18n.translate("vanished.player"), target.getDisplayName(), bool));
        });


        return true;
    }
}
