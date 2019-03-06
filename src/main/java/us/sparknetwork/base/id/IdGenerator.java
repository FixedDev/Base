package us.sparknetwork.base.id;

import com.google.common.util.concurrent.ListenableFuture;

public interface IdGenerator {
    long getNextId(String type);

    ListenableFuture<Long> getNextIdAsync(String type);
}
