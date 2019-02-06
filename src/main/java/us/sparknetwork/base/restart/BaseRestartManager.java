package us.sparknetwork.base.restart;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.Period;

public class BaseRestartManager implements RestartManager{
    @Override
    public void scheduleRestartAt(@NotNull LocalDateTime dateTime, @NotNull RestartPriority priority) {

    }

    @Override
    public void scheduleRestartIn(@NotNull Period period, @NotNull RestartPriority priority) {
        LocalDateTime now = LocalDateTime.now();

        scheduleRestartAt(now.plus(period), priority);
    }

    @Override
    public void cancelRestart() {

    }
}
