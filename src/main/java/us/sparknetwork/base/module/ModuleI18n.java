package us.sparknetwork.base.module;

import com.google.inject.Inject;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.inject.annotations.ModuleClassLoader;
import us.sparknetwork.base.inject.annotations.ModuleDataFolder;

import java.io.File;
import java.util.logging.Logger;

class ModuleI18n extends I18n {

    @Inject
    ModuleI18n(@ModuleDataFolder File dataFolder, Logger logger, @ModuleClassLoader ClassLoader classLoader) {
        super(dataFolder, logger, classLoader);
    }
}
