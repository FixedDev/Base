package us.sparknetwork.base.restart;

import lombok.Getter;

public enum RestartPriority {
    LOW(10), NORMAL(5), HIGH(0);

    @Getter
    private int maximumPlayers;

    RestartPriority(int maximumPlayers){
        this.maximumPlayers = maximumPlayers;
    }
}