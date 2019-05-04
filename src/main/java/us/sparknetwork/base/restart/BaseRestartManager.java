package us.sparknetwork.base.restart;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.server.ServerManager;
import us.sparknetwork.base.server.type.Server;
import me.fixeddev.service.AbstractService;
import us.sparknetwork.utils.DateUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

@Singleton
public class BaseRestartManager extends AbstractService implements RestartManager {

    @Inject
    private ServerManager serverManager;
    @Inject
    private Server localServer;
    @Inject
    private JavaPlugin plugin;
    @Inject
    private BukkitScheduler scheduler;
    @Inject
    private I18n i18n;

    private BukkitTask task;

    private List<Long> announceRestartTime = Arrays.asList(60L * 60, 30L * 60, 10L * 60, 5L * 60, 2L * 60, 60L, 30L);

    @Override
    public void scheduleRestartAt(@NotNull LocalDateTime dateTime, @NotNull RestartPriority priority) {
        Objects.requireNonNull(dateTime);
        Objects.requireNonNull(priority);

        localServer.setNextRestartDate(dateTime);
        localServer.setNextRestartPriority(priority);

        serverManager.save(localServer);
    }

    @Override
    public void scheduleRestartIn(@NotNull TemporalAmount period, @NotNull RestartPriority priority) {
        LocalDateTime now = LocalDateTime.now();

        scheduleRestartAt(now.plus(period), priority);
    }

    @Override
    public void cancelRestart() {
        localServer.setNextRestartDate(null);

        serverManager.save(localServer);
    }

    @Override
    protected void doStart() {
        task = scheduler.runTaskTimerAsynchronously(plugin, () -> {
            LocalDateTime now = LocalDateTime.now();

            LocalDateTime restartTime = localServer.getNextRestartDate();
            RestartPriority restartPriority = localServer.getNextRestartPriority();

            if (restartTime == null || restartPriority == null) {
                return;
            }

            int playersOnline = localServer.getOnlinePlayerNicks().size();
            long secondsLeft = now.until(restartTime, ChronoUnit.SECONDS);

            if (secondsLeft <= 0) {
                int restartMaximumPlayers = restartPriority.getMaximumPlayers();

                if (playersOnline <= restartMaximumPlayers || restartMaximumPlayers == -1) {
                    Bukkit.broadcastMessage(i18n.format("restart.now"));

                    scheduler.runTaskLater(plugin, () -> Bukkit.getServer().shutdown(), 2);
                    return;
                }

                Duration delayDuration = Duration.ofMinutes(1); // TODO: Make it configurable

                String prettyDuration = DateUtil.durationToPrettyDate(delayDuration, i18n);

                plugin.getLogger().log(Level.INFO, "The server restart was delayed {0} since the restart maximum players are {1} and server online players are {2}.",
                        new Object[]{prettyDuration, restartMaximumPlayers, playersOnline});

                restartTime = now.plus(Duration.ofMinutes(1));

                localServer.setNextRestartDate(restartTime);
                serverManager.save(localServer);

                Bukkit.broadcastMessage(i18n.format("restart.delayed", prettyDuration, restartPriority.getMaximumPlayers(), playersOnline));
                return;
            }

            if (secondsLeft <= 10 || announceRestartTime.contains(secondsLeft)) {
                Bukkit.broadcastMessage(i18n.format("restart.in", DateUtil.getHumanReadableDate(secondsLeft * 1000, i18n)));
            }
        }, 0, 20);
    }

    @Override
    protected void doStop() {
        task.cancel();
    }
}
