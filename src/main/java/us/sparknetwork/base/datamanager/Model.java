package us.sparknetwork.base.datamanager;


import com.fasterxml.jackson.annotation.JsonProperty;

public interface Model {
    @JsonProperty("_id")
    String getId();
}
