package us.sparknetwork.base.messager.impl;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import org.redisson.api.RedissonClient;
import us.sparknetwork.base.server.LocalServerData;
import us.sparknetwork.base.api.messager.Channel;
import us.sparknetwork.base.api.messager.Messenger;

import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class RedisMessenger implements Messenger {

    @Inject
    private RedissonClient redisson;
    @Inject
    private LocalServerData localServerData;
    @Inject
    private ListeningExecutorService executorService;

    private Map<TypeToken, Map<String, Channel>> channels = new ConcurrentHashMap<TypeToken, Map<String, Channel>>() {

        @Override
        public Map<String, Channel> get(Object key) {
            if (!(key instanceof TypeToken)) {
                return null;
            }

            Map<String, Channel> value = super.get(key);

            if (value == null) {
                value = new ConcurrentHashMap<>();

                put((TypeToken) key, value);
            }

            return value;
        }
    };

    @Override
    @SuppressWarnings("unchecked")
    public <T> Channel<T> getChannel(String channelName, TypeToken<T> type) {
        Channel<T> channel = (Channel<T>) channels.get(type).get(channelName);

        if (channel == null) {
            channel = new RedisChannel<>(channelName, type, redisson, executorService, localServerData);

            channels.get(type).put(channelName, channel);
        }

        return channel;
    }


}
