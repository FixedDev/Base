package us.sparknetwork.utils;

import com.google.inject.Singleton;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class TemporaryCommandUtils implements Listener {

    private Map<String, TemporalCommand> temporalCommandMap;
    private Map<UUID, List<String>> playerIdToCommandName;

    public TemporaryCommandUtils() {
        temporalCommandMap = new ConcurrentHashMap<>();
        playerIdToCommandName = new ConcurrentHashMap<>();
    }

    public void registerTemporalCommand(Player expectedSender, String name, TemporalCommand command) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(command);

        temporalCommandMap.put(name.toLowerCase(), command);

        List<String> commands = playerIdToCommandName.getOrDefault(expectedSender.getUniqueId(), new ArrayList<>());
        commands.add(name.toLowerCase());

        playerIdToCommandName.put(expectedSender.getUniqueId(), commands);
    }

    @EventHandler
    public void onPlayerPreProcess(PlayerCommandPreprocessEvent e) {
        String command = e.getMessage();

        if (!temporalCommandMap.containsKey(command.toLowerCase())) {
            return;
        }

        if (!playerIdToCommandName.get(e.getPlayer().getUniqueId()).contains(command.toLowerCase())) {
            return;
        }

        TemporalCommand temporalCommand = temporalCommandMap.get(command.toLowerCase());

        temporalCommand.run(e.getPlayer());

        temporalCommandMap.remove(command.toLowerCase());
        playerIdToCommandName.remove(e.getPlayer().getUniqueId());

        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e){
        List<String> commands = playerIdToCommandName.getOrDefault(e.getPlayer().getUniqueId(), new ArrayList<>());

        commands.forEach(temporalCommandMap::remove);
    }

    @FunctionalInterface
    public interface TemporalCommand {
        void run(Player player);
    }
}
