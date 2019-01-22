package us.sparknetwork.base.scoreboard.placeholders;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import me.clip.placeholderapi.PlaceholderAPI;
import me.ggamer55.scoreboard.placeholder.PlaceholderResolver;
import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.Set;

public class PlaceholderAPIResolver implements PlaceholderResolver {
    @Override
    public String replacePlaceholders(OfflinePlayer player, String text) {
        if(!player.isOnline()){
            return "";
        }
        return PlaceholderAPI.setPlaceholders(player.getPlayer(), text);
    }

    @Override
    public Set<String> getPlaceholders() {
        return ImmutableSet.copyOf(PlaceholderAPI.getRegisteredIdentifiers());
    }

    @Override
    public Map<String, String> getPlaceholdersValues(OfflinePlayer player) {
        return ImmutableMap.of();
    }
}
