package us.sparknetwork.base.command.essentials;

import com.google.common.base.Enums;
import com.google.inject.Inject;
import me.ggamer55.bcm.parametric.CommandClass;
import me.ggamer55.bcm.parametric.annotation.Command;
import me.ggamer55.bcm.parametric.annotation.Parameter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.sparknetwork.base.restart.RestartManager;
import us.sparknetwork.base.restart.RestartPriority;

import java.time.Duration;

public class RestartCommands implements CommandClass {
    @Inject
    private RestartManager restartManager;

    @Command(names = "restartnow", max = 0)
    public boolean restartNowCommand(@Parameter("sender") CommandSender sender, @Parameter(value = "priority", defaultValue = "HIGH") String stringPriority) {
        if (sender instanceof Player) {
            sender.sendMessage(ChatColor.RED + "No Permission.");
            return true;
        }

        RestartPriority priority = Enums.getIfPresent(RestartPriority.class, stringPriority).or(RestartPriority.HIGH);

        restartManager.scheduleRestartIn(Duration.ofSeconds(10), priority);
        return true;
    }
}
