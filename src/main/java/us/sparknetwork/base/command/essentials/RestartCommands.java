package us.sparknetwork.base.command.essentials;

import com.google.common.base.Enums;
import com.google.inject.Inject;
import me.ggamer55.bcm.parametric.CommandClass;
import me.ggamer55.bcm.parametric.annotation.Command;
import me.ggamer55.bcm.parametric.annotation.JoinedString;
import me.ggamer55.bcm.parametric.annotation.Parameter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.restart.RestartManager;
import us.sparknetwork.base.restart.RestartPriority;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class RestartCommands implements CommandClass {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Inject
    private RestartManager restartManager;
    @Inject
    private I18n i18n;

    @Command(names = "restartnow", max = 1, usage = "/<command> [priority]")
    public boolean restartNowCommand(@Parameter("sender") CommandSender sender, @Parameter(value = "priority", defaultValue = "HIGH") String stringPriority) {
        if (sender instanceof Player) {
            sender.sendMessage(ChatColor.RED + "No Permission.");
            return true;
        }

        RestartPriority priority = Enums.getIfPresent(RestartPriority.class, stringPriority).or(RestartPriority.HIGH);

        restartManager.scheduleRestartIn(Duration.ofSeconds(10), priority);
        return true;
    }

    @Command(names = "restartat", max = 2, min = 2, usage = "/<command> <date> <hour> [priority]")
    public boolean restartAtCommand(@Parameter("sender") CommandSender sender, @Parameter("date") @JoinedString(2) String date,  @Parameter(value = "priority", defaultValue = "LOW") String stringPriority) {
        if (sender instanceof Player) {
            sender.sendMessage(ChatColor.RED + "No Permission.");
            return true;
        }

        LocalDateTime dateTime;

        try {
            dateTime = LocalDateTime.parse(date, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ex) {
            return false;
        }

        RestartPriority priority = Enums.getIfPresent(RestartPriority.class, stringPriority).or(RestartPriority.LOW);

        restartManager.scheduleRestartAt(dateTime, priority);

        sender.sendMessage(i18n.format("restart.scheduled.at", dateTime.format(DATE_TIME_FORMATTER), priority));
        return true;
    }
}
