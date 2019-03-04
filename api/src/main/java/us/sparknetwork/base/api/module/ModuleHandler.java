package us.sparknetwork.base.api.module;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import us.sparknetwork.base.api.AbstractService;

import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class ModuleHandler extends AbstractService {

    @Getter
    private static Queue<BaseModule> loadedModules = new ConcurrentLinkedQueue<>();
    private static boolean isAcceptingModules = true;

    private List<BaseModule> registeredModules;
    private Logger logger;

    private Injector injector;

    @Inject
    ModuleHandler(Injector injector, Logger logger) {
        registeredModules = new CopyOnWriteArrayList<>();
        this.logger = logger;

        this.injector = injector;

        registerLoadedModules();
    }

    public static void loadModule(BaseModule module) {
        if (isAcceptingModules) {
            Objects.requireNonNull(module);
            loadedModules.add(module);

            return;
        }

        throw new IllegalStateException("Module loading is disabled now!");
    }

    @Override
    protected void doStart() {
        registeredModules.forEach(module -> {
            module.onEnable();
            logger.log(Level.INFO, "Enabled module {0}-{1}", new Object[]{module.name(), module.version()});
        });
    }

    @Override
    protected void doStop() {
        registeredModules.forEach(module -> {
            module.onDisable();
            logger.log(Level.INFO, "Disabled module {0}-{1}", new Object[]{module.name(), module.version()});
        });
    }

    private void registerLoadedModules() {
        isAcceptingModules = false;

        loadedModules.forEach(module -> {
            if (StringUtils.isBlank(module.name())) {
                logger.log(Level.WARNING, "Failed to register module {0}, because it doesn't have a name", module.getClass().getName());
                return;
            }

            if (module.version() == null || module.version().trim().isEmpty()) {
                logger.log(Level.WARNING, "Failed to register module {0}, because it doesn't have a version", module.name());
                return;
            }

            this.registeredModules.add(module);

            injector.injectMembers(module);

            logger.log(Level.INFO, "Loaded module {0}-{1}", new Object[]{module.name(), module.version()});
        });

        loadedModules.clear();
    }
}
