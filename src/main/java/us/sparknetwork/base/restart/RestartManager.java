package us.sparknetwork.base.restart;

import java.time.LocalDateTime;
import java.time.Period;

public interface RestartManager {
    void scheduleRestartAt(LocalDateTime dateTime, RestartPriority priority);

    void scheduleRestartIn(Period period, RestartPriority priority);

    void cancelRestart();

    void setMaximumUptime(Period period);


}
