package us.sparknetwork.base.api.datamanager;

import com.google.common.util.concurrent.ListenableFuture;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class DelegateStorageProvider<O extends Model> implements StorageProvider<O> {
    private StorageProvider<O> delegate;

    public DelegateStorageProvider(StorageProvider<O> delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull ListenableFuture<O> findOne(@NotNull String id) {
        return delegate.findOne(id);
    }

    @Override
    public @Nullable O findOneSync(@NotNull String id) {
        return delegate.findOneSync(id);
    }

    @Override
    public @NotNull ListenableFuture<Set<O>> find(@NotNull Set<String> ids, int limit) {
        return delegate.find(ids, limit);
    }

    @Override
    public @NotNull Set<O> findSync(@NotNull Set<String> ids, int limit) {
        return delegate.findSync(ids, limit);
    }

    @Override
    public @NotNull ListenableFuture<Set<O>> find(int limit) {
        return delegate.find(limit);
    }

    @Override
    public @NotNull Set<O> findSync(int limit) {
        return delegate.findSync(limit);
    }

    @Override
    public @NotNull ListenableFuture<Void> save(@NotNull O objects) {
        return delegate.save(objects);
    }

    @Override
    public @NotNull ListenableFuture<Void> save(@NotNull Set<O> objects) {
        return delegate.save(objects);
    }

    @Override
    public @NotNull ListenableFuture<O> findOneByQuery(Bson bsonQuery) {
        return delegate.findOneByQuery(bsonQuery);
    }

    @Override
    public O findOneByQuerySync(Bson bsonQuery) {
        return delegate.findOneByQuerySync(bsonQuery);
    }

    @Override
    public @NotNull ListenableFuture<Set<O>> findByQuery(Bson bsonQuery, int skip, int limit) {
        return delegate.findByQuery(bsonQuery, skip, limit);
    }

    @Override
    public @NotNull Set<O> findByQuerySync(Bson bsonQuery, int skip, int limit) {
        return delegate.findByQuerySync(bsonQuery, skip, limit);
    }

    @Override
    public @NotNull ListenableFuture<Void> delete(@NotNull O object) {
        return delegate.delete(object);
    }

    @Override
    public @NotNull ListenableFuture<Void> delete(@NotNull Set<O> objects) {
        return delegate.delete(objects);
    }

}
