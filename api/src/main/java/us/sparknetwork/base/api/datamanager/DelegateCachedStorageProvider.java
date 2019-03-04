package us.sparknetwork.base.api.datamanager;

import com.google.common.util.concurrent.ListenableFuture;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class DelegateCachedStorageProvider<O extends Model> extends DelegateStorageProvider<O> implements CachedStorageProvider<O> {

    private CachedStorageProvider<O> delegate;

    public DelegateCachedStorageProvider(CachedStorageProvider<O> delegate) {
        super(delegate);

        this.delegate = delegate;
    }

    @Override
    public ListenableFuture<Set<O>> findAllCached(int limit) {
        return delegate.findAllCached(limit);
    }

    @Override
    public Set<O> findAllCachedSync(int limit) {
        return delegate.findAllCachedSync(limit);
    }

    @Override
    public void refresh(String key) {
        delegate.refresh(key);
    }

    @Override
    public void refresh(Set<String> key) {
        delegate.refresh(key);
    }

    @Override
    public void invalidate(String key) {
        delegate.invalidate(key);
    }

    @Override
    public void invalidate(Set<String> key) {
        delegate.invalidate(key);
    }

    @Override
    public void invalidate() {
        delegate.invalidate();
    }

    @Override
    public ListenableFuture<Void> save(O objects, boolean force) {
        return delegate.save(objects, force);
    }

    @Override
    public ListenableFuture<Void> save(Set<O> objects, boolean force) {
        return delegate.save(objects, force);
    }

    @Override
    @NotNull
    public ListenableFuture<Void> save(@NotNull O objects) {
        return delegate.save(objects);
    }

    @Override
    @NotNull
    public ListenableFuture<Void> save(@NotNull Set<O> objects) {
        return delegate.save(objects);
    }


}
