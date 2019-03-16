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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MongoStorageProvider<O extends Model, P extends PartialModel> implements StorageProvider<O, P> {

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

    @Override
    public @NotNull ListenableFuture<O> findOneByQuery(Bson bsonQuery) {
        return executorService.submit(() -> mongoCollection.find(bsonQuery).first());
    }

    @Override
    public @Nullable O findOneByQuerySync(Bson bsonQuery) {
        return mongoCollection.find(bsonQuery).first();
    }

    @Override
    public ListenableFuture<List<O>> findByQuery(Bson bsonQuery, int skip, int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("Limit should be 1 or more!");
        }

        if (skip < 0) {
            throw new IllegalArgumentException("Skip should be 0 or more!");
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

        if (skip < 0) {
            throw new IllegalArgumentException("Skip should be 0 or more!");
        }

        List<O> objects = new ArrayList<>();

        mongoCollection.find(bsonQuery).skip(skip).limit(limit).into(objects);

        return objects;
    }

    @NotNull
    @Override
    public ListenableFuture<Void> save(@NotNull P o) {
        return executorService.submit(() -> {
            if (!(o instanceof Model)) {
                throw new IllegalArgumentException("The model to delete doesn't has a id field");
            }

            this.mongoCollection.replaceOne(createIdQuery(((Model) o).getId()), (O) o, new ReplaceOptions().upsert(true));
            return null;
        });
    }

    @NotNull
    @Override
    public ListenableFuture<Void> save(@NotNull Set<P> o) {
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
    public ListenableFuture<Void> delete(@NotNull P object) {
        if (!(object instanceof Model)) {
            throw new IllegalArgumentException("The model to delete doesn't has a id field");
        }

        return delete(((Model) object).getId());
    }

    public ListenableFuture<Void> delete(String id) {
        return executorService.submit(() -> {
            mongoCollection.findOneAndDelete(createIdQuery(id));

            return null;
        });
    }


    @NotNull
    @Override
    public ListenableFuture<Void> delete(@NotNull Set<P> objects) {
        return executorService.submit(() -> {

            for (P object : objects) {
                if (!(object instanceof Model)) {
                    throw new IllegalArgumentException("The model to delete doesn't has a id field");
                }

                mongoCollection.findOneAndDelete(createIdQuery(((Model) object).getId()));
            }

            return null;
        });
    }

    private Document createIdQuery(Object _id) {
        return new Document("_id", _id);
    }


}
