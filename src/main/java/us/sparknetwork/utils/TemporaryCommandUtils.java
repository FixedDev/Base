package us.sparknetwork.utils;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


public class TemporaryCommandUtils implements Listener {

    private Map<String, TemporalCommand> temporalCommandMap;

    public TemporaryCommandUtils(){
        temporalCommandMap = new ConcurrentHashMap<>();
    }

    public void registerTemporalCommand(String name, TemporalCommand command){
        Objects.requireNonNull(name);
        Objects.requireNonNull(command);

        temporalCommandMap.put(name, command);
    }

    @EventHandler
    public void onPlayerPreProcess(PlayerCommandPreprocessEvent e){
        String command = e.getMessage();

        if(!temporalCommandMap.containsKey(command)){
            return;
        }

        TemporalCommand temporalCommand = temporalCommandMap.get(command);

        temporalCommand.run(e.getPlayer());

        temporalCommandMap.remove(command);

        e.setCancelled(true);
    }

    @FunctionalInterface
    interface TemporalCommand {
        void run(Player player);
    }
}
