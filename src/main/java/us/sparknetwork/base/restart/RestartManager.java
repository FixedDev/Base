package us.sparknetwork.base.restart;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.Period;

public interface RestartManager {
    void scheduleRestartAt(@NotNull LocalDateTime dateTime, @NotNull RestartPriority priority);

    void scheduleRestartIn(@NotNull Period period, @NotNull RestartPriority priority);

    void cancelRestart();
}
