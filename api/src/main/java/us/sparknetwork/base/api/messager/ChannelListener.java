package us.sparknetwork.base.api.messager;

public interface ChannelListener<T> {
    void onMessageReceived(String channel, String serverSenderId, T data);
}
