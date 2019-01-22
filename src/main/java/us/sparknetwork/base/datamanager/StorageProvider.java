package us.sparknetwork.base.datamanager;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Set;

public interface StorageProvider<O extends Model> {

    ListenableFuture<O> findOne(String id);

    O findOneSync(String id);

    ListenableFuture<Set<O>> find(Set<String> ids, int limit);

    Set<O> findSync(Set<String> ids, int limit);

    ListenableFuture<Set<O>> find(int limit);

    Set<O> findSync(int limit);

    ListenableFuture<Void> save(O objects);

    ListenableFuture<Void> save(Set<O> objects);

    ListenableFuture<Void> delete(O object);

    ListenableFuture<Void> delete(Set<O> objects);
}
