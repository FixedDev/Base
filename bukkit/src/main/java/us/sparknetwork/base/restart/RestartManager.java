package us.sparknetwork.base.restart;

import org.jetbrains.annotations.NotNull;
import us.sparknetwork.base.Service;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.TemporalAmount;

public interface RestartManager extends Service {
    void scheduleRestartAt(@NotNull LocalDateTime dateTime, @NotNull RestartPriority priority);

    void scheduleRestartIn(@NotNull TemporalAmount period, @NotNull RestartPriority priority);

    void cancelRestart();
}
