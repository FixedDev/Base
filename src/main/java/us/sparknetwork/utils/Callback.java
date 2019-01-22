package us.sparknetwork.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

public interface Callback<T> {

    void call(T object);

    default void handleException(Throwable throwable){
        Logger.getGlobal().log(Level.SEVERE, "Something went wrong when executing a callback", throwable);
    }
}
