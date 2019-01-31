package us.sparknetwork.base.datamanager;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redisson.api.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MongoStorageProvider<O extends Model> implements StorageProvider<O> {

    private ListeningExecutorService executorService;

    private MongoCollection<O> mongoCollection;

    private String dataPrefix;
    private Class<? extends O> modelClazz;


    public MongoStorageProvider(ListeningExecutorService executorService, MongoDatabase database, String dataPrefix, Class<? extends O> modelClazz) {
        this.executorService = executorService;
        this.dataPrefix = dataPrefix;
        this.modelClazz = modelClazz;

        mongoCollection = database.getCollection(dataPrefix, (Class<O>) modelClazz);
    }

    @NotNull
    @Override
    public ListenableFuture<O> findOne(@NotNull String id) {
        return executorService.submit(() -> mongoCollection.find(createIdQuery(id)).first());
    }

    @Nullable
    @Override
    public O findOneSync(@NotNull String id) {
        return mongoCollection.find(createIdQuery(id)).first();
    }

    @NotNull
    @Override
    public ListenableFuture<Set<O>> find(@NotNull Set<String> ids, int limit) {
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

                O object = mongoCollection.find(createIdQuery(id)).first();

                if (object != null) {
                    objects.add(object);
                }

            }

            return objects;
        });
    }

    @NotNull
    @Override
    public Set<O> findSync(@NotNull Set<String> ids, int limit) {
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

            O object = mongoCollection.find(createIdQuery(id)).first();

            if (object != null) {
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

        return objects;
    }

    @NotNull
    @Override
    public ListenableFuture<Void> save(@NotNull O o) {
        return executorService.submit(() -> {
            this.mongoCollection.replaceOne(createIdQuery(o.getId()), o, new ReplaceOptions().upsert(true));
            return null;
        });
    }

    @NotNull
    @Override
    public ListenableFuture<Void> save(@NotNull Set<O> o) {
        return executorService.submit(() -> {
            Set<O> toSaveInMongoDB = new HashSet<>();


            toSaveInMongoDB.forEach(o1 -> {
                ReplaceOptions options = new ReplaceOptions();
                options.upsert(true);

                this.mongoCollection.replaceOne(createIdQuery(o1.getId()), o1, options);
            });

            return null;
        });
    }

    @NotNull
    @Override
    public ListenableFuture<Void> delete(@NotNull O object) {
        return delete(object.getId());
    }

    public ListenableFuture<Void> delete(String id) {
        return executorService.submit(() -> {
            mongoCollection.findOneAndDelete(createIdQuery(id));

            return null;
        });
    }


    @NotNull
    @Override
    public ListenableFuture<Void> delete(@NotNull Set<O> objects) {
        return executorService.submit(() -> {

            for (O object : objects) {
                mongoCollection.findOneAndDelete(createIdQuery(object.getId()));
            }

            return null;
        });
    }

    private Document createIdQuery(Object _id) {
        return new Document("_id", _id);
    }


}
