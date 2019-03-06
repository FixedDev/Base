package us.sparknetwork.base.punishment;

import com.google.inject.AbstractModule;

public class PunishmentManagerModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(PunishmentManager.class).to(BasePunishmentManager.class);
    }
}
