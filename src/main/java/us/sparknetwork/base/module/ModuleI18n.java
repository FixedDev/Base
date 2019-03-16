package us.sparknetwork.base.module;

import us.sparknetwork.base.I18n;
import us.sparknetwork.base.inject.annotations.ModuleDataFolder;

import java.io.File;
import java.util.logging.Logger;

public class ModuleI18n extends I18n {

    public ModuleI18n(@ModuleDataFolder File dataFolder, Logger logger) {
        super(dataFolder, logger);
    }
}
