package us.sparknetwork.base.inject;

import com.google.inject.AbstractModule;
import us.sparknetwork.base.server.MongoServerManager;
import us.sparknetwork.base.server.ServerManager;
import us.sparknetwork.base.user.BaseUserHandler;
import us.sparknetwork.base.user.UserHandler;
import us.sparknetwork.base.user.finder.UserFinder;
import us.sparknetwork.base.user.finder.impl.UserFinderImpl;
import us.sparknetwork.base.whisper.WhisperManager;
import us.sparknetwork.base.whisper.WhisperManagerImpl;
import us.sparknetwork.base.itemdb.impl.ItemDb;
import us.sparknetwork.base.itemdb.SimpleItemDb;
import us.sparknetwork.base.messager.Messenger;
import us.sparknetwork.base.messager.impl.RedisMessenger;

public class HandlersModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Messenger.class).to(RedisMessenger.class);
        bind(ServerManager.class).to(MongoServerManager.class);
        bind(UserFinder.class).to(UserFinderImpl.class);
        bind(ItemDb.class).to(SimpleItemDb.class);
        bind(UserHandler.class).to(BaseUserHandler.class);
        bind(WhisperManager.class).to(WhisperManagerImpl.class);
    }
}
