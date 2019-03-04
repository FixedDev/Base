package us.sparknetwork.base.api.messager;

import com.google.gson.reflect.TypeToken;

public interface Messenger {

    <T> Channel<T> getChannel(String channelName, TypeToken<T> type);

    default <T> Channel<T> getChannel(String channelName, Class<T> type){
        return getChannel(channelName, TypeToken.get(type));
    }

}
