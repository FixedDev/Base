package us.sparknetwork.base.messager.impl;

import lombok.AllArgsConstructor;
import org.redisson.api.listener.MessageListener;
import us.sparknetwork.base.handlers.server.Server;
import us.sparknetwork.base.messager.ChannelListener;

import java.util.Objects;

@AllArgsConstructor
class RedissonChannelWrapper<T> implements MessageListener<ObjectWrapper<T>> {

    private String channelName;
    private Server serverData;
    private ChannelListener<T> messageListener;

    @Override
    public void onMessage(CharSequence channel, ObjectWrapper<T> object) {
        if (!channel.equals(channelName)) {
            return;
        }

        if (object.getServerSenderId().equals(serverData.getId())) {
            return;
        }

        messageListener.onMessageReceived(channel.toString(), object.getServerSenderId(), object.getObject());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RedissonChannelWrapper)) return false;
        RedissonChannelWrapper wrapper = (RedissonChannelWrapper) o;
        return Objects.equals(messageListener, wrapper.messageListener);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageListener);
    }
}