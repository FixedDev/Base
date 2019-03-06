package us.sparknetwork.base.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import us.sparknetwork.base.punishment.Punishment;

@AllArgsConstructor
public class PunishmentEvent extends Event {
    private static HandlerList handlerList = new HandlerList();

    @Getter
    private Punishment punishment;

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }}
