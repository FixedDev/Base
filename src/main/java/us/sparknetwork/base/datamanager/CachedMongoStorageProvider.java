package us.sparknetwork.base.datamanager;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redisson.api.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class CachedMongoStorageProvider<O extends Model, P extends PartialModel> implements CachedStorageProvider<O, P> {

    private ListeningExecutorService executorService;

    private RedissonClient redissonClient;
    private MongoCollection<O> mongoCollection;

    private String dataPrefix;
    private Class<? extends O> modelClazz;

    public CachedMongoStorageProvider(ListeningExecutorService executorService, MongoDatabase database, RedissonClient redisson, String dataPrefix, Class<O> modelClazz) {
        this.executorService = executorService;
        this.redissonClient = redisson;
        this.dataPrefix = dataPrefix;
        this.modelClazz = modelClazz;

        mongoCollection = database.getCollection(dataPrefix, modelClazz);
    }

    @NotNull
    @Override
    public ListenableFuture<O> findOne(@NotNull String id) {
        return executorService.submit(() -> findOneSync(id));
    }

    @Nullable
    @Override
    public O findOneSync(@NotNull String id) {
        O object;

        RBucket<O> rBucket = redissonClient.getBucket(dataPrefix + ":" + id);

        if (rBucket.isExists()) {
            return rBucket.get();
        }

        object = mongoCollection.find(createIdQuery(id)).first();

        if (object != null) {
            rBucket.set(object);
            rBucket.expire(2, TimeUnit.MINUTES);
        }

        return object;
    }

    @NotNull
    @Override
    public ListenableFuture<Set<O>> find(@NotNull Set<String> ids) {
        return executorService.submit(() -> findSync(ids));
    }

    @NotNull
    @Override
    public Set<O> findSync(@NotNull Set<String> ids) {
        Set<O> objects = new HashSet<>();

        Iterator<String> idIterator = ids.iterator();

        while (idIterator.hasNext()) {
            String id = idIterator.next();
            RBucket<O> rBucket = redissonClient.getBucket(dataPrefix + ":" + id);

            if (rBucket.isExists()) {
                objects.add(rBucket.get());

                continue;
            }

            O object = mongoCollection.find(createIdQuery(id)).first();

            if (object != null) {
                rBucket.set(object);
                rBucket.expire(2, TimeUnit.MINUTES);

                objects.add(object);
            }

        }

        return objects;
    }

    @NotNull
    @Override
    public ListenableFuture<Set<O>> find(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("The specified limit must be 0 or more");
        }
        if (limit == 0) {
            return Futures.immediateFuture(Sets.newHashSet());
        }

        return executorService.submit(() -> {
            Set<O> objects = new HashSet<>();

            mongoCollection.find().limit(limit).into(objects);

            if (objects.isEmpty()) {
                objects.addAll(findAllCachedSync(limit));
            }

            return objects;
        });
        //return executorService.submit(() -> new HashSet<>(advancedDatastore.find(dataPrefix, modelClazz).asList(new FindOptions().limit(limit))));
    }

    @NotNull
    @Override
    public Set<O> findSync(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("The specified limit must be 0 or more");
        }
        if (limit == 0) {
            return Sets.newHashSet();
        }

        Set<O> objects = new HashSet<>();

        mongoCollection.find().limit(limit).into(objects);

        if (objects.isEmpty()) {
            objects.addAll(findAllCachedSync(limit));
        }

        return objects;
    }


    @Override
    public @NotNull
    ListenableFuture<O> findOneByQuery(Bson bsonQuery) {
        return executorService.submit(() -> mongoCollection.find(bsonQuery).first());
    }

    @Override
    @Nullable
    public O findOneByQuerySync(Bson bsonQuery) {
        return mongoCollection.find(bsonQuery).first();
    }

    @Override
    public ListenableFuture<List<O>> findByQuery(Bson bsonQuery, int skip, int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("Limit should be 1 or more!");
        }

        return executorService.submit(() -> {
            List<O> objects = new ArrayList<>();

            mongoCollection.find(bsonQuery).skip(skip).limit(limit).into(objects);

            return objects;
        });
    }

    @Override
    public List<O> findByQuerySync(Bson bsonQuery, int skip, int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("Limit should be 1 or more!");
        }

        List<O> objects = new ArrayList<>();

        mongoCollection.find(bsonQuery).skip(skip).limit(limit).into(objects);

        return objects;
    }


    @Override
    public ListenableFuture<Void> save(P o, boolean force) {
        return executorService.submit(() -> {
            if (!(o instanceof Model)) {
                throw new IllegalArgumentException("The model to save doesn't has a id field");
            }

            O object = (O) o;

            RBucket<O> rBucket = redissonClient.getBucket(dataPrefix + ":" + object.getId());

            rBucket.set(object);
            rBucket.expire(2, TimeUnit.MINUTES);

            this.mongoCollection.replaceOne(createIdQuery(object.getId()), object, new ReplaceOptions().upsert(true));

            return null;
        });
    }

    @Override
    public ListenableFuture<Void> save(Set<P> o, boolean force) {
        return executorService.submit(() -> {
            RBatch rBatch = redissonClient.createBatch(BatchOptions.defaults());

            o.forEach(object -> {
                if (!(object instanceof Model)) {
                    throw new IllegalArgumentException("The model to save doesn't has a id field");
                }

                O completeObject = (O) object;

                RBucketAsync<O> rBucket = rBatch.getBucket(dataPrefix + ":" + completeObject.getId());
                rBucket.setAsync(completeObject);
                rBucket.expireAsync(2, TimeUnit.MINUTES);
            });

            rBatch.execute();

            o.forEach(object -> {
                if (!(object instanceof Model)) {
                    throw new IllegalArgumentException("The model to save doesn't has a id field");
                }

                ReplaceOptions options = new ReplaceOptions();
                options.upsert(true);

                O completeObject = (O) object;

                this.mongoCollection.replaceOne(createIdQuery(completeObject.getId()), completeObject, options);
            });

            return null;
        });
    }

    @NotNull
    @Override
    public ListenableFuture<Void> delete(@NotNull P object) {
        if (!(object instanceof Model)) {
            throw new IllegalArgumentException("The model to delete doesn't has a id field");
        }

        return delete(((Model) object).getId());
    }

    public ListenableFuture<Void> delete(String id) {
        return executorService.submit(() -> {
            RBucket<O> rBucket = redissonClient.getBucket(dataPrefix + ":" + id);
            rBucket.delete();

            mongoCollection.findOneAndDelete(createIdQuery(id));

            return null;
        });
    }


    @NotNull
    @Override
    public ListenableFuture<Void> delete(@NotNull Set<P> objects) {
        return executorService.submit(() -> {
            RBatch rBatch = redissonClient.createBatch(BatchOptions.defaults());

            for (P object : objects) {
                if (!(object instanceof Model)) {
                    throw new IllegalArgumentException("The model to delete doesn't has a id field");
                }

                O completeModel = (O) object;

                RBucketAsync<O> rBucket = rBatch.getBucket(dataPrefix + ":" + completeModel.getId());
                rBucket.deleteAsync();

                mongoCollection.findOneAndDelete(createIdQuery(completeModel.getId()));
            }

            rBatch.execute();

            return null;
        });
    }

    @Override
    public ListenableFuture<Set<O>> findAllCached(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("The specified limit must be 0 or more");
        }
        if (limit == 0) {
            return Futures.immediateFuture(Sets.newHashSet());
        }

        return executorService.submit(() -> {
            Set<O> cachedObjects = new HashSet<>();

            int keysCount = (int) Math.min(redissonClient.getKeys().count(), limit);

            redissonClient.getKeys().getKeysByPattern(dataPrefix + ":*", keysCount).forEach(s -> {
                RBucket<O> rBucket = redissonClient.getBucket(dataPrefix + ":" + s);

                cachedObjects.add(rBucket.get());
            });

            return cachedObjects;
        });
    }

    @Override
    public Set<O> findAllCachedSync(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("The specified limit must be 0 or more");
        }
        if (limit == 0) {
            return Sets.newHashSet();
        }

        Set<O> cachedObjects = new HashSet<>();

        int keysCount = (int) Math.min(redissonClient.getKeys().count(), limit);

        redissonClient.getKeys().getKeysByPattern(dataPrefix + ":*", keysCount).forEach(s -> {
            RBucket<O> rBucket = redissonClient.getBucket(dataPrefix + ":" + s);

            cachedObjects.add(rBucket.get());
        });

        return cachedObjects;
    }

    @Override
    public void refresh(String key) {
        executorService.submit(() -> {
            O object = findOneSync(key);

            if (object == null) {
                return;
            }

            RBucket<O> rBucket = redissonClient.getBucket(dataPrefix + ":" + object.getId());
            rBucket.set(object);

            RMap<String, Integer> rMap = redissonClient.getMap("count:" + dataPrefix);
            rMap.fastPut(key, 0);
        });
    }

    @Override
    public void refresh(Set<String> keys) {
        executorService.submit(() -> {
            keys.forEach(s -> {
                O object = findOneSync(s);

                if (object == null) {
                    return;
                }

                RBucket<O> rBucket = redissonClient.getBucket(dataPrefix + ":" + s);
                rBucket.set(object);

                RMap<String, Integer> rMap = redissonClient.getMap("count:" + dataPrefix);
                rMap.fastPut(s, 0);
            });
        });
    }


    @Override
    public void invalidate(String key) {
        executorService.submit(() -> {
            RBucket<O> rBucket = redissonClient.getBucket(dataPrefix + ":" + key);
            rBucket.delete();
        });
    }

    @Override
    public void invalidate(Set<String> keys) {
        executorService.submit(() -> {
            RBatch rBatch = redissonClient.createBatch(BatchOptions.defaults());
            keys.forEach(s -> {

                RBucketAsync<O> rBucket = rBatch.getBucket(dataPrefix + ":" + s);
                rBucket.deleteAsync();
            });

            rBatch.execute();
        });
    }

    @Override
    public void invalidate() {
        executorService.submit(() -> {
            RBatch rBatch = redissonClient.createBatch(BatchOptions.defaults());

            rBatch.getKeys().countAsync().whenComplete((count, throwable) -> {
                redissonClient.getKeys().getKeysByPattern(dataPrefix + ":*", (int) Math.min(count, Integer.MAX_VALUE)).forEach(s -> {
                    RBucketAsync<O> rBucket = rBatch.getBucket(dataPrefix + ":" + s);
                    rBucket.deleteAsync();
                });
            });

            rBatch.execute();
        });
    }

    private Document createIdQuery(Object _id) {
        return new Document("_id", _id);
    }


}
