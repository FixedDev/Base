package us.sparknetwork.utils;

import com.google.common.base.Function;
import com.google.common.util.concurrent.*;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

public class ListenableFutureUtils {

    public static <V, V1> ListenableFuture<BiSupplier<V, V1>> combine(ListenableFuture<V> futureA, ListenableFuture<V1> futureB) {
        AsyncFunction<V, BiSupplier<V, V1>> function = value -> {
            return Futures.transform(futureB, value2 -> {
                return new BiSupplier<V, V1>() {
                    @Override
                    public V getFirst() {
                        return value;
                    }

                    @Override
                    public V1 getSecond() {
                        return value2;
                    }
                };
            }, MoreExecutors.newDirectExecutorService());
        };

        return Futures.transformAsync(futureA, function, MoreExecutors.newDirectExecutorService());
    }

    public static <T, R> ListenableFuture<R> transformFuture(ListenableFuture<T> future, Function<T, R> function) {
        return Futures.transform(future, function, MoreExecutors.directExecutor());
    }

    public static <T, R> ListenableFuture<R> transformFutureAsync(ListenableFuture<T> future, AsyncFunction<T, R> function, ExecutorService executor) {
        return Futures.transformAsync(future, function, executor);
    }

    public static <T> ListenableFuture<Optional<T>> addOptionalToReturnValue(ListenableFuture<T> future) {
        return Futures.transform(future, (Function<T, Optional<T>>) Optional::ofNullable, MoreExecutors.newDirectExecutorService());
    }

    public static <T> void addCallback(ListenableFuture<T> futureUtils, Callback<T> callback) {
        Futures.addCallback(futureUtils, wrapCallback(callback), MoreExecutors.newDirectExecutorService());
    }

    public static <T> FutureCallback<T> wrapCallback(Callback<T> callback) {
        return new FutureCallback<T>() {
            @Override
            public void onSuccess(@Nullable T t) {
                callback.call(t);
            }

            @Override
            public void onFailure(Throwable throwable) {
                callback.handleException(throwable);
            }
        };
    }
}
