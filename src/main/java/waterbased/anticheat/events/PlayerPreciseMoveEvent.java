package waterbased.anticheat.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerPreciseMoveEvent extends Event implements Cancellable {

    public static HandlerList handlers = new HandlerList();
    private final Player player;
    private final Location from;
    private final Location to;
    private final boolean onGround;
    private boolean cancelled;

    public PlayerPreciseMoveEvent(Player p, Location from, Location to, boolean onGround) {
        this.player = p;
        this.from = from;
        this.to = to;
        this.onGround = onGround;
    }

    public Player getPlayer() {
        return player;
    }

    public Location getFrom() {
        return from.clone();
    }

    public Location getTo() {
        return to.clone();
    }

    public boolean isClientOnGround() {
        return this.onGround;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

}
