package waterbased.anticheat.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ServerTickEvent extends Event {

    public static HandlerList handlers = new HandlerList();
    private final long tick;

    public ServerTickEvent(long tick) {
        this.tick = tick;
    }

    public long getTick() {
        return this.tick;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
