package waterbased.anticheat.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerPreciseMoveEvent extends Event {

    public static HandlerList handlers = new HandlerList();

    private Player player;
    private Location from;
    private Location to;
    private boolean onGround;

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
        return from;
    }

    public Location getTo() {
        return to;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
