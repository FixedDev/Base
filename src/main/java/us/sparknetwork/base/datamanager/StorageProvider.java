package us.sparknetwork.base.datamanager;

import com.google.common.util.concurrent.ListenableFuture;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public interface StorageProvider<O extends Model> {

    @NotNull
    ListenableFuture<O> findOne(@NotNull String id);

    @Nullable
    O findOneSync(@NotNull String id);

    @NotNull
    ListenableFuture<Set<O>> find(@NotNull Set<String> ids, int limit);

    @NotNull
    Set<O> findSync(@NotNull Set<String> ids, int limit);

    @NotNull
    ListenableFuture<Set<O>> find(int limit);

    @NotNull
    Set<O> findSync(int limit);

    @NotNull
    ListenableFuture<Void> save(@NotNull O objects);

    @NotNull
    ListenableFuture<Void> save(@NotNull Set<O> objects);

    @NotNull ListenableFuture<O> findOneByQuery(Bson bsonQuery);

    O findOneByQuerySync(Bson bsonQuery);

    ListenableFuture<List<O>> findByQuery(Bson bsonQuery, int skip, int limit);

    List<O> findByQuerySync(Bson bsonQuery, int skip, int limit);

    @NotNull
    ListenableFuture<Void> delete(@NotNull O object);

    @NotNull
    ListenableFuture<Void> delete(@NotNull Set<O> objects);
}
