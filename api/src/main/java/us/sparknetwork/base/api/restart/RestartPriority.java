package us.sparknetwork.base.api.restart;

import lombok.Getter;

public enum RestartPriority {
    LOW(10), NORMAL(20), HIGH(-1);

    @Getter
    private int maximumPlayers;

    RestartPriority(int maximumPlayers){
        this.maximumPlayers = maximumPlayers;
    }
}