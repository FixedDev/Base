package us.sparknetwork.base.module;

public interface ModuleInfo {
    String name();
    String version();

    String[] authors();

    ClassLoader getClassLoader();
}
