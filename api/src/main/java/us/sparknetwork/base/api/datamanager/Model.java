package us.sparknetwork.base.api.datamanager;


import com.fasterxml.jackson.annotation.JsonProperty;

public interface Model {
    @JsonProperty("_id")
    String getId();
}
