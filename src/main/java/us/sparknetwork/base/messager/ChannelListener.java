package us.sparknetwork.base.messager;

import java.util.UUID;

public interface ChannelListener<T> {
    void onMessageReceived(String channel, String serverSenderId, T data);
}
