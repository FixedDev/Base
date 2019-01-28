package us.sparknetwork.base.server;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;

import com.google.inject.Singleton;
import com.mongodb.client.MongoDatabase;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import us.sparknetwork.base.datamanager.MongoStorageProvider;
import us.sparknetwork.utils.ListenableFutureUtils;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class MongoServerManager extends MongoStorageProvider<Server> implements ServerManager, Listener {

    private ListeningExecutorService executorService;

    private AtomicBoolean started;

    @Inject
    private LocalServerData serverData;

    private JavaPlugin plugin;

    private Instant lastUpdate;

    @Inject
    MongoServerManager(ListeningExecutorService executorService, MongoDatabase database, JavaPlugin plugin) {
        super(executorService, database, "server", Server.class);

        this.executorService = executorService;

        started = new AtomicBoolean();

        this.plugin = plugin;
    }

    @Override
    public void start() throws IllegalStateException {
        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException("The service is already started");
        }

        executorService.submit(() -> ListenableFutureUtils.addCallback(find(Integer.MAX_VALUE), servers -> {
            long serversWithSameName = servers.stream()
                    .filter(server -> serverData.getId().equals(server.getId()))
                    .count();

            if (serversWithSameName > 1) {
                Bukkit.getLogger().severe("Well, this is an unexpected error.");
                Bukkit.getLogger().severe("There's another server with the same id, change the id and restart the server.");
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "/stop");
            }
        }));

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if(lastUpdate == null) {
                lastUpdate = Instant.now();

                save(serverData);
                return;
            }

            long secondsSinceLastUpdate = Instant.now().getEpochSecond() - lastUpdate.getEpochSecond();

            if (secondsSinceLastUpdate <= 3) {
                return;
            }

            save(serverData);

            lastUpdate = Instant.now();
        }, 100, 100);
    }

    @Override
    public void stop() {
        if (!started.compareAndSet(true, false)) {
            throw new IllegalStateException("The service isn't already started");
        }

        serverData.setOnline(false);
        serverData.setStartedAt(null);

        this.save(serverData);
    }

    @Override
    public boolean isStarted() {
        return started.get();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        lastUpdate = Instant.now();

        this.save(serverData);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Bukkit.getScheduler().runTaskLater(plugin, () ->{
            lastUpdate = Instant.now();

            this.save(serverData);
        },1);
    }
}
