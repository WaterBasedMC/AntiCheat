package waterbased.anticheat.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerOnGroundChangeEvent extends Event {

    public static HandlerList handlers = new HandlerList();

    private final boolean onGround;
    private final Player player;
    private final double fallDistance;

    public PlayerOnGroundChangeEvent(Player player, boolean onGround, double fallDistance) {
        this.player = player;
        this.onGround = onGround;
        this.fallDistance = fallDistance;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public Player getPlayer() {
        return this.player;
    }

    public double getFallDistance() {
        return this.fallDistance;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
