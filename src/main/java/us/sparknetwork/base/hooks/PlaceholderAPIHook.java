package us.sparknetwork.base.hooks;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.google.inject.Injector;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.server.Server;
import us.sparknetwork.base.server.ServerManager;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class PlaceholderAPIHook {

    public static void hookPlaceholders(Injector injector) {
        injector.getInstance(ServerCountPlacerholders.class).register();
        injector.getInstance(ServerOnlinePlaceholder.class).register();
        injector.getInstance(ServerWhitelistPlaceholder.class).register();
    }

    static class ServerCountPlacerholders extends PlaceholderExpansion {

        private Map<String, String> playerCountResponseCache = new ConcurrentHashMap<>();
        private Map<String, Long> playerCountLastUpdate = new ConcurrentHashMap<>();

        @Inject
        private JavaPlugin plugin;
        @Inject
        private I18n i18n;
        @Inject
        private ServerManager serverManager;

        @Override
        public String getIdentifier() {
            return "server-count";
        }

        @Override
        public String getPlugin() {
            return plugin.getName();
        }

        @Override
        public String getAuthor() {
            return "Ggamer55";
        }

        @Override
        public String getVersion() {
            return plugin.getDescription().getVersion();
        }

        @Override
        public String onPlaceholderRequest(Player player, String identifier) {
            if (identifier.equalsIgnoreCase("total")) {
                if ((System.currentTimeMillis() - playerCountLastUpdate.getOrDefault("total", 0L) >= 2500)) {
                    recacheAllPlayerCounts();
                }

                return playerCountResponseCache.get("total");

            } else {
                if ((System.currentTimeMillis() - playerCountLastUpdate.getOrDefault(identifier, 0L)) >= 2500) {
                    recachePlayerCount(identifier);
                }

                return playerCountResponseCache.get(identifier);
            }
        }

        private void addPlayerCountResponseToCache(String serverId, String response) {
            playerCountResponseCache.put(serverId, response);
            playerCountLastUpdate.put(serverId, System.currentTimeMillis());
        }

        private void recachePlayerCount(String serverId) {
            Futures.addCallback(serverManager.findOne(serverId), new FutureCallback<Server>() {
                @Override
                public void onSuccess(@Nullable Server server) {
                    if (server == null) {
                        addPlayerCountResponseToCache(serverId, i18n.translate("unknown"));
                        return;
                    }


                    if (!server.isOnline()) {
                        addPlayerCountResponseToCache(server.getId(), i18n.translate("offline"));
                        return;
                    }

                    addPlayerCountResponseToCache(server.getId(), server.getOnlinePlayerIds().size() + "");
                }

                @Override
                public void onFailure(Throwable throwable) {
                    plugin.getLogger().log(Level.SEVERE, "An error occurred when getting server's player count", throwable);
                }
            }, MoreExecutors.newDirectExecutorService());
        }

        private void recacheAllPlayerCounts() {
            Futures.addCallback(serverManager.find(Integer.MAX_VALUE), new FutureCallback<Set<Server>>() {
                @Override
                public void onSuccess(@Nullable Set<Server> servers) {
                    final AtomicInteger playerCount = new AtomicInteger();

                    if (servers == null) {
                        return;
                    }

                    servers.parallelStream()
                            .forEach(server -> {
                                if (!server.isOnline()) {
                                    addPlayerCountResponseToCache(server.getId(), i18n.translate("offline"));
                                    return;
                                }

                                int serverPlayerCount = server.getOnlinePlayerIds().size();

                                addPlayerCountResponseToCache(server.getId(), serverPlayerCount + "");

                                playerCount.addAndGet(serverPlayerCount);
                            });

                    addPlayerCountResponseToCache("total", playerCount.intValue() + "");
                }

                @Override
                public void onFailure(Throwable throwable) {
                    plugin.getLogger().log(Level.SEVERE, "An error occurred when getting server's player count", throwable);
                }
            }, MoreExecutors.newDirectExecutorService());
        }
    }

    static class ServerOnlinePlaceholder extends PlaceholderExpansion {

        private Map<String, String> serverOnlineResponseCache = new ConcurrentHashMap<>();
        private Map<String, Long> serverOnlineLastUpdate = new ConcurrentHashMap<>();

        @Inject
        private JavaPlugin plugin;
        @Inject
        private I18n i18n;
        @Inject
        private ServerManager serverManager;


        @Override
        public String getIdentifier() {
            return "server-online";
        }

        @Override
        public String getPlugin() {
            return plugin.getName();
        }

        @Override
        public String getAuthor() {
            return "Ggamer55";
        }

        @Override
        public String getVersion() {
            return plugin.getDescription().getVersion();
        }

        @Override
        public String onPlaceholderRequest(Player player, String identifier) {
            if ((System.currentTimeMillis() - serverOnlineLastUpdate.getOrDefault(identifier, 0L)) >= 2500) {
                recacheServerOnline(identifier);
            }

            return serverOnlineResponseCache.get(identifier);
        }

        private void addServerOnlineResponseToCache(String serverId, String response) {
            serverOnlineResponseCache.put(serverId, response);
            serverOnlineLastUpdate.put(serverId, System.currentTimeMillis());
        }

        private void recacheServerOnline(String serverId) {
            Futures.addCallback(serverManager.findOne(serverId), new FutureCallback<Server>() {
                @Override
                public void onSuccess(@Nullable Server server) {
                    if (server == null) {
                        addServerOnlineResponseToCache(serverId, i18n.translate("unknown"));
                        return;
                    }

                    addServerOnlineResponseToCache(server.getId(), server.isOnline() ? i18n.translate("online") : i18n.translate("offline"));
                }

                @Override
                public void onFailure(Throwable throwable) {
                    plugin.getLogger().log(Level.SEVERE, "An error occurred when getting server's online status", throwable);
                }
            }, MoreExecutors.newDirectExecutorService());
        }
    }

    static class ServerWhitelistPlaceholder extends PlaceholderExpansion {
        private Map<String, String> serverWhitelistResponseCache = new ConcurrentHashMap<>();
        private Map<String, Long> serverWhitelistLastUpdate = new ConcurrentHashMap<>();

        @Inject
        private JavaPlugin plugin;
        @Inject
        private I18n i18n;
        @Inject
        private ServerManager serverManager;


        @Override
        public String getIdentifier() {
            return "server-whitelisted";
        }

        @Override
        public String getPlugin() {
            return plugin.getName();
        }

        @Override
        public String getAuthor() {
            return "Ggamer55";
        }

        @Override
        public String getVersion() {
            return plugin.getDescription().getVersion();
        }

        @Override
        public String onPlaceholderRequest(Player player, String identifier) {
            if ((System.currentTimeMillis() - serverWhitelistLastUpdate.getOrDefault(identifier, 0L)) >= 2500) {
                recacheServerWhitelist(identifier);
            }

            return serverWhitelistResponseCache.get(identifier);
        }

        private void addServerWhitelistResponseToCache(String serverId, String response) {
            serverWhitelistResponseCache.put(serverId, response);
            serverWhitelistLastUpdate.put(serverId, System.currentTimeMillis());
        }

        private void recacheServerWhitelist(String serverId) {
            Futures.addCallback(serverManager.findOne(serverId), new FutureCallback<Server>() {
                @Override
                public void onSuccess(@Nullable Server server) {
                    if (server == null) {
                        addServerWhitelistResponseToCache(serverId, i18n.translate("unknown"));
                        return;
                    }

                    addServerWhitelistResponseToCache(server.getId(), server.isOnline() ? i18n.translate("whitelisted") : i18n.translate("unwhitelisted"));
                }

                @Override
                public void onFailure(Throwable throwable) {
                    plugin.getLogger().log(Level.SEVERE, "An error occurred when getting server's whitelist status", throwable);
                }
            }, MoreExecutors.newDirectExecutorService());
        }

    }

}
