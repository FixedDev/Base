package us.sparknetwork.base.module;

import com.google.inject.AbstractModule;
import us.sparknetwork.utils.inject.ProtectedBinder;

import java.util.Collection;

public class ModuleHandlerModule extends AbstractModule {

    private Collection<BaseModule> moduleList;

    public ModuleHandlerModule(Collection<BaseModule> moduleList) {
        this.moduleList = moduleList;
    }

    @Override
    protected void configure() {
        moduleList.forEach(module -> {
            ProtectedBinder.newProtectedBinder(binder())
                    .install(module);
        });

        bind(ModuleHandler.class);
    }

}
