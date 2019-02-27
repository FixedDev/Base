package us.sparknetwork.base.restart;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.TemporalAmount;

public interface RestartManager {
    void scheduleRestartAt(@NotNull LocalDateTime dateTime, @NotNull RestartPriority priority);

    void scheduleRestartIn(@NotNull TemporalAmount period, @NotNull RestartPriority priority);

    void cancelRestart();
}
