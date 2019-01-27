package us.sparknetwork.base.handlers.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;


public interface Identity {
    @JsonIgnore
    @NotNull
    UUID getUUID();

    @JsonIgnore
    @NotNull
    String getLastName();

    @NotNull
    List getNameHistory();

    void tryAddName(String var1);

    @Nullable
    String getNick();

    default boolean hasNick() {
        return this.getNick() != null;
    }

    void setNick(@Nullable String var1);
}
