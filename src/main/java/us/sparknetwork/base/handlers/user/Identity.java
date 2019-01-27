package us.sparknetwork.base.handlers.user;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.UUID;

public interface Identity {
    @JsonIgnore
    UUID getUUID();

    @JsonIgnore
    String getLastName();

    List<String> getNameHistory();

    void tryAddName(String name);

    String getNick();
}
