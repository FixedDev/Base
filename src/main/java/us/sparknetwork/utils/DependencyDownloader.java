package us.sparknetwork.utils;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;

import org.bukkit.Bukkit;

public class DependencyDownloader {
    private static Method classLoaderAddUrl;

    private DependencyDownloader() {
    }

    public static void downloadAndAddToClasspath(URL url, File destinyFile) {
        if (!destinyFile.exists()) {
            try {
                if (!destinyFile.createNewFile()) {
                    downloadFile(url, destinyFile);
                }
            } catch (IOException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to download file from " + url.toString() + " exception: ", ex);
                return;
            }
        }

        try {
            addJarToClasspath(destinyFile);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to add file from " + destinyFile.getName() + " to classpath, exception: ", ex);
        }
    }

    private static void downloadFile(URL url, File destinyFile) throws IOException {
        InputStream stream = url.openStream();

        try (ReadableByteChannel channel = Channels.newChannel(stream); FileOutputStream fileStream = new FileOutputStream(destinyFile)) {
            fileStream.getChannel().transferFrom(channel, 0L, Long.MAX_VALUE);
            fileStream.flush();
        }
    }

    public static void addFolderJarsToClassPath(File folder) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, MalformedURLException {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return;
        }

        for (File file : folder.listFiles()) {
            addJarToClasspath(file);
        }
    }

    public static void addJarToClasspath(File jar) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, MalformedURLException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        if (classLoaderAddUrl == null) {
            Class<?> clazz = classLoader.getClass();

            Method method = clazz.getSuperclass().getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);

            classLoaderAddUrl = method;
        }

        classLoaderAddUrl.invoke(classLoader, jar.toURI().toURL());
    }
}
