package us.sparknetwork.base.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import us.sparknetwork.base.user.User;

public class UserVanishEvent extends Event {
    private static HandlerList handlerList = new HandlerList();

    private User.State user;
    private boolean oldVanishState;
    private boolean newVanishState;

    public UserVanishEvent(User.State user, boolean oldVanishState, boolean newVanishState) {
        this.user = user;
        this.oldVanishState = oldVanishState;
        this.newVanishState = newVanishState;
    }

    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
