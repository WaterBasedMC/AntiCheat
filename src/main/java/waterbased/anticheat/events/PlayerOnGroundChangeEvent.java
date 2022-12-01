package waterbased.anticheat.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerOnGroundChangeEvent extends Event {

    public static HandlerList handlers = new HandlerList();

    private final boolean onGround;
    private final Player player;

    public PlayerOnGroundChangeEvent(Player player, boolean onGround) {
        this.player = player;
        this.onGround = onGround;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public Player getPlayer() {
        return this.player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
