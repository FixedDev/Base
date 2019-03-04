package us.sparknetwork.base.api.messager;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.reflect.TypeToken;

public interface Channel<T> {

    String getName();

    TypeToken<T> getType();

    ListenableFuture<Void> sendMessage(T data);

    boolean hasListeners();

    void registerListener(ChannelListener<T> listener);

    void unregisterListener(ChannelListener<T> listener);
}
