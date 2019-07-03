package us.sparknetwork.base.datamanager;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redisson.api.RBucket;
import us.sparknetwork.base.redis.RedisExecutor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RedisStorageProvider<C extends Model, P extends PartialModel> implements StorageProvider<C, P> {

    private static final String QUERY_NOT_SUPPORTED = "You can't make bson queries on a redis database!";

    private RedisExecutor redisExecutor;

    private String dataPrefix;

    public RedisStorageProvider(RedisExecutor redisExecutor, String dataPrefix) {
        this.redisExecutor = redisExecutor;
        this.dataPrefix = dataPrefix;
    }

    @NotNull
    @Override
    public ListenableFuture<C> findOne(@NotNull String id) {
        return redisExecutor.submit(redisson ->
                redisson.<C>getBucket(dataPrefix + ":" + id).get()
        );
    }

    @Nullable
    @Override
    public C findOneSync(@NotNull String id) {
        return redisExecutor.submitSync(redisson ->
                redisson.<C>getBucket(dataPrefix + ":" + id).get()
        );
    }

    @NotNull
    @Override
    public ListenableFuture<Set<C>> find(@NotNull Set<String> ids) {
        return redisExecutor.submit(redisson ->
                ids.stream()
                        .map(s -> dataPrefix + ":" + s)
                        .map(redisson::<C>getBucket)
                        .map(RBucket::get)
                        .collect(Collectors.toSet())
        );
    }

    @NotNull
    @Override
    public Set<C> findSync(@NotNull Set<String> ids) {
        return redisExecutor.submitSync(redisson ->
                ids.stream()
                        .map(s -> dataPrefix + ":" + s)
                        .map(redisson::<C>getBucket)
                        .map(RBucket::get)
                        .collect(Collectors.toSet())
        );
    }

    @NotNull
    @Override
    public ListenableFuture<Set<C>> find(int limit) {
        return redisExecutor.submit(redisson -> {
            String[] ids = Iterables.toArray(redisson.getKeys().getKeysByPattern(dataPrefix + ":*"), String.class);

            return new HashSet<>(redisson.getBuckets().<C>get(ids).values());
        });
    }

    @NotNull
    @Override
    public Set<C> findSync(int limit) {
        return redisExecutor.submitSync(redisson -> {
            String[] ids = Iterables.toArray(redisson.getKeys().getKeysByPattern(dataPrefix + ":*"), String.class);

            return new HashSet<>(redisson.getBuckets().<C>get(ids).values());
        });
    }

    @NotNull
    @Override
    public ListenableFuture<Void> save(@NotNull P object) {
        if (!(object instanceof Model)) {
            throw new IllegalArgumentException("The model to save doesn't has a id field");
        }

        C modelObject = (C) object;

        return redisExecutor.submit(redisson -> {
            redisson.<C>getBucket(dataPrefix + ":" + modelObject.getId()).set(modelObject);
            return null;
        });
    }

    @NotNull
    @Override
    public ListenableFuture<Void> save(@NotNull Set<P> objects) {
        if (objects.stream().anyMatch(p -> !(p instanceof Model))) {
            throw new IllegalArgumentException("One of the specified models doesn't has a id field");
        }

        return redisExecutor.submit(redisson -> {
            objects.stream().map(p -> (C) p).forEach(modelObject -> {
                redisson.<C>getBucket(dataPrefix + ":" + modelObject.getId()).set(modelObject);
            });

            return null;
        });
    }

    @NotNull
    @Override
    public ListenableFuture<C> findOneByQuery(Bson bsonQuery) {
        return Futures.immediateFailedFuture(new UnsupportedOperationException(QUERY_NOT_SUPPORTED));
    }

    @Override
    public C findOneByQuerySync(Bson bsonQuery) {
        throw new UnsupportedOperationException(QUERY_NOT_SUPPORTED);
    }

    @Override
    public ListenableFuture<List<C>> findByQuery(Bson bsonQuery, int skip, int limit) {
        return Futures.immediateFailedFuture(new UnsupportedOperationException(QUERY_NOT_SUPPORTED));
    }

    @Override
    public List<C> findByQuerySync(Bson bsonQuery, int skip, int limit) {
        throw new UnsupportedOperationException(QUERY_NOT_SUPPORTED);
    }

    @NotNull
    @Override
    public ListenableFuture<Void> delete(@NotNull P object) {
        if (!(object instanceof Model)) {
            throw new IllegalArgumentException("The model to delete doesn't has a id field");
        }

        C modelObject = (C) object;

        return redisExecutor.submit(redisson -> {
            redisson.<C>getBucket(dataPrefix + ":" + modelObject.getId()).delete();
            return null;
        });
    }

    @NotNull
    @Override
    public ListenableFuture<Void> delete(@NotNull Set<P> objects) {
        if (objects.stream().anyMatch(p -> !(p instanceof Model))) {
            throw new IllegalArgumentException("One of the specified models doesn't has a id field");
        }

        return redisExecutor.submit(redisson -> {
            objects.stream().map(p -> (C) p).forEach(modelObject -> {
                redisson.<C>getBucket(dataPrefix + ":" + modelObject.getId()).delete();
            });

            return null;
        });
    }
}
