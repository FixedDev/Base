package us.sparknetwork.base.task;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public interface TickableTask {
    default Duration getTickPeriod() {
        return Duration.of(1, ChronoUnit.SECONDS);
    }

    void tickTask();

}
