package us.sparknetwork.base.task;

import java.util.Set;

public interface TaskManager {

    boolean isTaskRegistered(TickableTask task);

    void registerTask(TickableTask task);

    void unregisterTask(TickableTask task);

    Set<TickableTask> getActiveTasks();
}
