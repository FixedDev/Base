package us.sparknetwork.base.datamanager;


import com.fasterxml.jackson.annotation.JsonProperty;

public interface Model extends PartialModel {
    @JsonProperty("_id")
    String getId();
}
