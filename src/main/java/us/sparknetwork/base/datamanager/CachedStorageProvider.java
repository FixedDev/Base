package us.sparknetwork.base.datamanager;

import com.google.common.util.concurrent.ListenableFuture;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface CachedStorageProvider<O extends Model> extends StorageProvider<O> {

    ListenableFuture<Set<O>> findAllCached(int limit);

    Set<O> findAllCachedSync(int limit);

    void refresh(String key);

    void refresh(Set<String> key);

    void invalidate(String key);

    void invalidate(Set<String> key);

    void invalidate();

    ListenableFuture<Void> save(O objects, boolean force);

    ListenableFuture<Void> save(Set<O> objects, boolean force);

    @NotNull
    @Override
    default ListenableFuture<Void> save(@NotNull O objects) {
        return save(objects, false);
    }

    @NotNull
    @Override
    default ListenableFuture<Void> save(@NotNull Set<O> objects) {
        return save(objects, false);
    }
}

