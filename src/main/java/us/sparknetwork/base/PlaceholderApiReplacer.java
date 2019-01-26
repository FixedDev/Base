package us.sparknetwork.base;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PlaceholderApiReplacer {

    public static String parsePlaceholders(Player player, String text){
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    public static String parseRelationalPlaceholders(Player player1, Player player2, String text){
        return PlaceholderAPI.setRelationalPlaceholders(player1, player2, text);
    }
}
