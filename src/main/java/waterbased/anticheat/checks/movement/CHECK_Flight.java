package waterbased.anticheat.checks.movement;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import waterbased.anticheat.events.PlayerOnGroundChangeEvent;
import waterbased.anticheat.events.PlayerPreciseMoveEvent;
import waterbased.anticheat.utils.Notifier;
import waterbased.anticheat.utils.Punishment;
import waterbased.anticheat.utils.UtilCheat;

import java.util.HashMap;

public class CHECK_Flight implements Listener {

    private static HashMap<Player, Location> lastGround = new HashMap<>();
    private static final HashMap<Player, Integer> onGroundGrace = new HashMap<>();


    @EventHandler
    public void onMove(PlayerPreciseMoveEvent e) {
        if(UtilCheat.isOnGround(e.getTo(), 1) || e.getPlayer().getAllowFlight()) {
            lastGround.put(e.getPlayer(), e.getTo());
            onGroundGrace.put(e.getPlayer(), 0);
            return;
        }
        if(e.getTo().getY() - lastGround.get(e.getPlayer()).getY() > 2.5) { //TODO: Consider JumpBoost Effect
            e.getPlayer().teleport(lastGround.get(e.getPlayer()));
            Punishment.freeze(e.getPlayer());
        }
    }

    @EventHandler
    public void onGroundChange(PlayerOnGroundChangeEvent e) {
        if(e.isOnGround() && !UtilCheat.isOnGround(e.getPlayer().getLocation())) {
            int grace = onGroundGrace.getOrDefault(e.getPlayer(), 0);
            grace ++;
            if(grace >= 3) {
                Punishment.pullDown(e.getPlayer());
                Notifier.notify(Notifier.Check.MOVEMENT_Flight, e.getPlayer(), "t: OnGroundInAir");
                onGroundGrace.remove(e.getPlayer());
            } else {
                onGroundGrace.put(e.getPlayer(), grace);
            }
        }
    }


}
