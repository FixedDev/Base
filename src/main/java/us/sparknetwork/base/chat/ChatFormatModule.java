package us.sparknetwork.base.chat;

import com.google.inject.Scopes;
import me.fixeddev.inject.ServiceBinder;
import us.sparknetwork.utils.inject.ProtectedModule;

public class ChatFormatModule extends ProtectedModule {
    @Override
    protected void configure() {
        bind(ChatFormatManager.class).to(BaseChatFormatManager.class).in(Scopes.SINGLETON);

        ServiceBinder serviceBinder = new ServiceBinder(binder());
        serviceBinder.bindService(BaseChatFormatManager.class, Scopes.SINGLETON);
    }
}