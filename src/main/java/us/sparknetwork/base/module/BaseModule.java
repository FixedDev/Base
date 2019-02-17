package us.sparknetwork.base.module;

import com.google.inject.Singleton;
import us.sparknetwork.utils.inject.ProtectedModule;

@Singleton
public abstract class BaseModule extends ProtectedModule implements ModuleInfo {

    public void onEnable() {
    }

    public void onDisable() {
    }
}
