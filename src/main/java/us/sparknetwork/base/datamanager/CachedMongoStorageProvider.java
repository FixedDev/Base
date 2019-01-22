package us.sparknetwork.base.datamanager;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.redisson.api.*;

import java.util.*;

public class CachedMongoStorageProvider<O extends Model> implements CachedStorageProvider<O> {

    private ListeningExecutorService executorService;

    private RedissonClient redissonClient;
    private MongoCollection<O> mongoCollection;

    private String dataPrefix;
    private Class<? extends O> modelClazz;


    public CachedMongoStorageProvider(ListeningExecutorService executorService, MongoDatabase database, RedissonClient redisson, String dataPrefix, Class<? extends O> modelClazz) {
        this.executorService = executorService;
        this.redissonClient = redisson;
        this.dataPrefix = dataPrefix;
        this.modelClazz = modelClazz;

        mongoCollection = database.getCollection(dataPrefix, (Class<O>) modelClazz);
    }

    @Override
    public ListenableFuture<O> findOne(String id) {
        return executorService.submit(() -> {
            O object;

            RBucket<O> rBucket = redissonClient.getBucket(dataPrefix + ":" + id);

            if (rBucket.isExists()) {
                return rBucket.get();
            }

            object = mongoCollection.find(createIdQuery(id)).first();

            if (object != null) {
                rBucket.set(object);
            }

            return object;
        });
    }

    @Override
    public O findOneSync(String id) {
        O object;

        RBucket<O> rBucket = redissonClient.getBucket(dataPrefix + ":" + id);

        if (rBucket.isExists()) {
            return rBucket.get();
        }

        object = mongoCollection.find(createIdQuery(id)).first();

        if (object != null) {
            rBucket.set(object);
        }

        return object;
    }

    @Override
    public ListenableFuture<Set<O>> find(Set<String> ids, int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("The specified limit must be 0 or more");
        }
        if (limit == 0) {
            return Futures.immediateFuture(Sets.newHashSet());
        }
        return executorService.submit(() -> {
            Set<O> objects = new HashSet<>();

            Iterator<String> idIterator = ids.iterator();

            for (int i = 0; i <= limit && idIterator.hasNext(); i++) {
                String id = idIterator.next();
                RBucket<O> rBucket = redissonClient.getBucket(dataPrefix + ":" + id);

                if (rBucket.isExists()) {
                    objects.add(rBucket.get());

                    continue;
                }

                O object = mongoCollection.find(createIdQuery(id)).first();

                if (object != null) {
                    rBucket.set(object);

                    objects.add(object);
                }

            }

            return objects;
        });
    }

    @Override
    public Set<O> findSync(Set<String> ids, int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("The specified limit must be 0 or more");
        }
        if (limit == 0) {
            return Sets.newHashSet();
        }

        Set<O> objects = new HashSet<>();

        Iterator<String> idIterator = ids.iterator();

        for (int i = 0; i <= limit && idIterator.hasNext(); i++) {
            String id = idIterator.next();
            RBucket<O> rBucket = redissonClient.getBucket(dataPrefix + ":" + id);

            if (rBucket.isExists()) {
                objects.add(rBucket.get());

                continue;
            }

            O object = mongoCollection.find(createIdQuery(id)).first();

            if (object != null) {
                rBucket.set(object);

                objects.add(object);
            }

        }

        return objects;
    }

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

            if(objects.isEmpty()){
                objects.addAll(findAllCachedSync(limit));
            }

            return objects;
        });
        //return executorService.submit(() -> new HashSet<>(advancedDatastore.find(dataPrefix, modelClazz).asList(new FindOptions().limit(limit))));
    }

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

        if(objects.isEmpty()){
            objects.addAll(findAllCachedSync(limit));
        }

        return objects;
    }

    @Override
    public ListenableFuture<Void> save(O o, boolean force) {
        return executorService.submit(() -> {
            RBucket<O> rBucket = redissonClient.getBucket(dataPrefix + ":" + o.getId());
            rBucket.set(o);

            RMap<String, Integer> cacheWriteCount = redissonClient.getMap("count:" + dataPrefix);

            int writeCount = cacheWriteCount.addAndGet(o.getId(), 1);

            if ((writeCount % 3) == 0 || force) {
                this.mongoCollection.replaceOne(createIdQuery(o.getId()), o, new ReplaceOptions().upsert(true));
            }

            return null;
        });
    }

    @Override
    public ListenableFuture<Void> save(Set<O> o, boolean force) {
        return executorService.submit(() -> {
            Set<O> toSaveInMongoDB = new HashSet<>();
            RBatch rBatch = redissonClient.createBatch(BatchOptions.defaults());

            o.forEach(object -> {
                RBucketAsync<O> rBucket = rBatch.getBucket(dataPrefix + ":" + object.getId());
                rBucket.setAsync(object);

                RMapAsync<String, Integer> cacheWriteCount = rBatch.getMap("count:" + dataPrefix);

                RFuture<Integer> writeCount = cacheWriteCount.addAndGetAsync(object.getId(), 1);

                writeCount.whenComplete((integer, throwable) -> {
                    if ((integer % 3) == 0 || force) {
                        toSaveInMongoDB.add(object);
                    }
                });
            });

            rBatch.execute();

            toSaveInMongoDB.forEach(o1 -> {
                ReplaceOptions options = new ReplaceOptions();
                options.upsert(true);


                this.mongoCollection.replaceOne(createIdQuery(o1.getId()), o1, options);

            });

            return null;
        });
    }

    @Override
    public ListenableFuture<Void> delete(O object) {
        return delete(object.getId());
    }

    public ListenableFuture<Void> delete(String id) {
        return executorService.submit(() -> {

            RBucket<O> rBucket = redissonClient.getBucket(dataPrefix + ":" + id);
            rBucket.delete();

            RMap<String, Integer> cacheWriteCount = redissonClient.getMap("count:" + dataPrefix);
            cacheWriteCount.remove(id);

            mongoCollection.findOneAndDelete(createIdQuery(id));

            return null;
        });
    }


    @Override
    public ListenableFuture<Void> delete(Set<O> objects) {
        return executorService.submit(() -> {
            RBatch rBatch = redissonClient.createBatch(BatchOptions.defaults());

            for (O object : objects) {
                RBucketAsync<O> rBucket = rBatch.getBucket(dataPrefix + ":" + object.getId());
                rBucket.deleteAsync();

                RMapAsync<String, Integer> rMap = rBatch.getMap("count:" + dataPrefix);
                rMap.removeAsync(object.getId());

                mongoCollection.findOneAndDelete(createIdQuery(object.getId()));
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

            RMap<String, Integer> rMap = redissonClient.getMap("count:" + dataPrefix);
            rMap.fastRemove(key);
        });
    }

    @Override
    public void invalidate(Set<String> keys) {
        executorService.submit(() -> {
            RBatch rBatch = redissonClient.createBatch(BatchOptions.defaults());
            keys.forEach(s -> {

                RBucketAsync<O> rBucket = rBatch.getBucket(dataPrefix + ":" + s);
                rBucket.deleteAsync();

                RMapAsync<String, Integer> rMap = rBatch.getMap("count:" + dataPrefix);
                rMap.fastRemoveAsync(s);
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

                    RMapAsync<String, Integer> rMap = rBatch.getMap("count:" + dataPrefix);
                    rMap.fastRemoveAsync(s);
                });
            });

            rBatch.execute();
        });
    }

    private Document createIdQuery(Object _id) {
        return new Document("_id", _id);
    }


}
