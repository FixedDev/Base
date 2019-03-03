package us.sparknetwork.base.messager;

public interface ChannelListener<T> {
    void onMessageReceived(String channel, String serverSenderId, T data);
}
