package us.sparknetwork.base.task.impl;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import us.sparknetwork.base.task.TaskManager;
import us.sparknetwork.base.task.TickableTask;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class TaskManagerImpl implements TaskManager {

    private Map<Duration, TickableTask> taskMap;
    private Map<TickableTask, LocalDateTime> taskNextExecutionMap;

    @Inject
    public TaskManagerImpl(JavaPlugin plugin) {
        taskMap = new ConcurrentHashMap<>();
        taskNextExecutionMap = new ConcurrentHashMap<>();

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::tickAllTasks, 1, 1);
    }

    private void tickAllTasks() {
        for (Map.Entry<Duration, TickableTask> entry : taskMap.entrySet()) {
            Duration tickPeriod = entry.getKey();
            TickableTask tickableTask = entry.getValue();

            if (!tickPeriod.equals(tickableTask.getTickPeriod())) {
                taskMap.remove(tickPeriod);

                taskMap.put(tickableTask.getTickPeriod(), tickableTask);

                continue;
            }

            if (!taskNextExecutionMap.containsValue(tickableTask)) {
                LocalDateTime nextExecution = LocalDateTime.now().plus(tickPeriod);
                if (!nextExecution.isAfter(LocalDateTime.now())) {
                    continue;
                }

                tickableTask.tickTask();

                taskNextExecutionMap.put(tickableTask, nextExecution);
                continue;
            }

            LocalDateTime nextExecution = taskNextExecutionMap.get(tickableTask);

            if (nextExecution.isAfter(LocalDateTime.now())) {
                continue;
            }

            nextExecution = nextExecution.plus(tickPeriod);

            tickableTask.tickTask();

            taskNextExecutionMap.put(tickableTask, nextExecution);

        }
    }

    @Override
    public boolean isTaskRegistered(TickableTask task) {
        return taskMap.containsValue(task);
    }

    @Override
    public void registerTask(TickableTask task) {
        Preconditions.checkArgument(isTaskRegistered(task), "You can't register 2 times the same task!");

        taskMap.put(task.getTickPeriod(), task);
    }

    @Override
    public void unregisterTask(TickableTask task) {
        Preconditions.checkArgument(!isTaskRegistered(task), "You can't unregister a task that isn't registered!");

        taskMap.remove(task.getTickPeriod());
        taskNextExecutionMap.remove(task);
    }

    @Override
    public Set<TickableTask> getActiveTasks() {
        return new HashSet<>(taskMap.values());
    }
}
