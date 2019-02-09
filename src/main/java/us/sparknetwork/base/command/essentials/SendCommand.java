package us.sparknetwork.base.command.essentials;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import me.ggamer55.bcm.parametric.CommandClass;
import me.ggamer55.bcm.parametric.annotation.Command;
import me.ggamer55.bcm.parametric.annotation.Parameter;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import us.sparknetwork.base.I18n;

public class SendCommand implements CommandClass {

    @Inject
    private JavaPlugin plugin;
    @Inject
    private I18n i18n;

    @Command(names = "send", usage = "/<command> <player> <server>", min = 2, max = 2, permission = "base.command.send")
    public boolean sendCommand(@Parameter("sender") CommandSender sender, @Parameter("player") OfflinePlayer player, @Parameter(value = "server") String server) {
        if (!player.isOnline()) {
            sender.sendMessage(i18n.format("offline.player", player.getName()));
            return true;
        }

        Player target = player.getPlayer();

        target.sendMessage(i18n.format("player.server.sending", server, sender.getName()));
        sendToServer(target, server);

        sender.sendMessage(i18n.format("player.server.send", target.getName(), server));

        return true;
    }

    private void sendToServer(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("Connect");
        out.writeUTF(server);

        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }
}
