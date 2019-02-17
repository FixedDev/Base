package us.sparknetwork.test.base.module;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.junit.Test;
import us.sparknetwork.base.module.BaseModule;
import us.sparknetwork.base.module.ModuleHandler;
import us.sparknetwork.base.module.ModuleHandlerModule;

import java.util.logging.Logger;

public class ModuleHandlerTest {

    @Test
    public void testModuleRegister() {
        ModuleHandler.loadModule(new TestModule());
    }

    @Test
    public void testModuleEnabling() {
        Injector injector = Guice.createInjector(new ModuleHandlerModule(ModuleHandler.getLoadedModules()));

        ModuleHandler moduleHandler = injector.getInstance(ModuleHandler.class);

        moduleHandler.start();
    }

    static class TestModule extends BaseModule {

        @Inject
        private FakeBinding fakeBinding;

        @Override
        public String name() {
            return "test";
        }

        @Override
        public String version() {
            return "1.0-SNAPSHOT";
        }

        @Override
        public String[] authors() {
            return new String[0];
        }

        @Override
        protected void configure() {
            bind(FakeBinding.class).to(FakeBindingImplementation.class);
            expose(FakeBinding.class);
        }

        @Override
        public void onEnable() {
            fakeBinding.hola();
        }
    }

    interface FakeBinding {
        void hola();
    }

    static class FakeBindingImplementation implements FakeBinding {
        @Inject
        private Logger logger;

        @Override
        public void hola() {
            logger.info("Fake binding binded!");
        }
    }
}
