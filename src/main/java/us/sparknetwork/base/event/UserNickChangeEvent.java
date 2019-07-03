package us.sparknetwork.base.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;
import us.sparknetwork.base.user.User;

public class UserNickChangeEvent extends Event {
    private static HandlerList handlerList = new HandlerList();

    private User.Complete user;
    @Nullable
    private String oldNick;
    @Nullable
    private String newNick;

    public UserNickChangeEvent(User.Complete user, @Nullable String oldNick, @Nullable String newNick) {
        this.user = user;
        this.oldNick = oldNick;
        this.newNick = newNick;
    }

    public User getUser() {
        return this.user;
    }

    @Nullable
    public String getOldNick() {
        return this.oldNick;
    }

    @Nullable
    public String getNewNick() {
        return this.newNick;
    }

    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
