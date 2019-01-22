package us.sparknetwork.base.messager;

import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.java.JavaPlugin;
import us.sparknetwork.base.BasePlugin;

import javax.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public interface Messenger {

    <T> Channel<T> getChannel(String channelName, TypeToken<T> type);

    default <T> Channel<T> getChannel(String channelName, Class<T> type){
        return getChannel(channelName, TypeToken.get(type));
    }

}
