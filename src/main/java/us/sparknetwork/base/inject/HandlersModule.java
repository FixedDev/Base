package us.sparknetwork.base.inject;

import com.google.inject.AbstractModule;
import us.sparknetwork.base.handlers.server.MongoServerManager;
import us.sparknetwork.base.handlers.server.ServerManager;
import us.sparknetwork.base.handlers.user.data.UserDataHandler;
import us.sparknetwork.base.handlers.user.finder.UserFinder;
import us.sparknetwork.base.handlers.user.finder.impl.UserFinderImpl;
import us.sparknetwork.base.handlers.user.settings.UserSettingsHandler;
import us.sparknetwork.base.handlers.user.settings.UserSettingsHandlerImpl;
import us.sparknetwork.base.handlers.user.state.UserStateHandler;
import us.sparknetwork.base.handlers.whisper.WhisperManager;
import us.sparknetwork.base.handlers.whisper.WhisperManagerImpl;
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
        bind(UserStateHandler.class);
        bind(UserSettingsHandler.class).to(UserSettingsHandlerImpl.class);
        bind(UserDataHandler.class);
        bind(WhisperManager.class).to(WhisperManagerImpl.class);
    }
}
