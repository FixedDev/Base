package us.sparknetwork.base.messager.impl;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import us.sparknetwork.base.server.LocalServerData;
import us.sparknetwork.base.messager.Channel;
import us.sparknetwork.base.messager.ChannelListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class RedisChannel<T> implements Channel<T> {

    @Getter
    private String name;
    @Getter
    private TypeToken<T> type;

    private Queue<RedissonChannelWrapper<T>> registeredListeners;

    private Map<ChannelListener<T>, RedissonChannelWrapper<T>> channelWrapperMap;

    private RedissonClient redisson;
    private ListeningExecutorService executorService;
    private LocalServerData serverData;

    RedisChannel(String channelName, TypeToken<T> type, RedissonClient pool, ListeningExecutorService executorService, LocalServerData serverData) {
        this.name = channelName;
        this.type = type;

        this.redisson = pool;
        this.executorService = executorService;
        this.serverData = serverData;

        this.registeredListeners = new ConcurrentLinkedDeque<>();
        this.channelWrapperMap = new ConcurrentHashMap<>();
    }


    @Override
    public void registerListener(ChannelListener<T> listener) {
        Objects.requireNonNull(listener, "ChannelListener must be not null");

        RedissonChannelWrapper<T> wrapper = new RedissonChannelWrapper<>(name, serverData, listener);

        RTopic rTopic = redisson.getTopic(name);

        rTopic.addListener(new TypeToken<ObjectWrapper<T>>() {
        }.getRawType(), wrapper);

        registeredListeners.offer(wrapper);
        channelWrapperMap.put(listener, wrapper);
    }


    @Override
    public void unregisterListener(ChannelListener<T> listener) {
        Objects.requireNonNull(listener, "ChannelListener must be not null");

        RedissonChannelWrapper<T> wrapper = channelWrapperMap.get(listener);

        Objects.requireNonNull(wrapper, "The provided listener is not registered");

        registeredListeners.remove(wrapper);
    }

    @Override
    public ListenableFuture<Void> sendMessage(T data) {
        return executorService.submit(() -> {
            RTopic rTopic = redisson.getTopic(name);

            rTopic.publish(new ObjectWrapper<T>(data, serverData.getId()));

            return null;
        });

    }

    @Override
    public boolean hasListeners() {
        return !registeredListeners.isEmpty();
    }


}
