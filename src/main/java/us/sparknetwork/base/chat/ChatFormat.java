package us.sparknetwork.base.chat;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

public interface ChatFormat extends ConfigurationSerializable {
    String getName();

    int getPriority();

    String getChatFormat();

    String getPermission();

    boolean isUsePlaceholderApi();

    boolean isAllowRelationalPlaceholders();

    void setPriority(int priority);

    void setChatFormat(String chatFormat);

    void setPermission(String permission);

    void setUsePlaceholderApi(boolean usePlaceholderApi);

    void setAllowRelationalPlaceholders(boolean allowRelationalPlaceholders);
}
