package us.sparknetwork.base.chat;

import com.google.inject.AbstractModule;

public class ChatFormatModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(BaseChatFormatManager.class).to(BaseChatFormatManager.class);
    }
}
