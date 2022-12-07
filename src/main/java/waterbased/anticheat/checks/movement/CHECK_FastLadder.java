package waterbased.anticheat.checks.movement;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import waterbased.anticheat.events.PlayerPreciseMoveEvent;
import waterbased.anticheat.checks.Check;
import waterbased.anticheat.utils.Notifier;
import waterbased.anticheat.utils.Punishment;

import java.util.HashMap;

public class CHECK_FastLadder implements Listener {

    private static double MAX_Y_DIFF_PER_TICK = 0.131;

    double max = 0;

    private HashMap<Player, Long> lastClimbTime = new HashMap<>();
    private HashMap<Player, Location> lastClimbLoc = new HashMap<>();

    @EventHandler
    public void onMove(PlayerPreciseMoveEvent e) {

        if(!e.getPlayer().getName().equals("HerrVergesslich")) return;

        if(e.getPlayer().getAllowFlight()) return;

        if(PlayerMovement.isOnGround(e.getPlayer()) || !PlayerMovement.isClimbing(e.getPlayer())) {
            lastClimbTime.remove(e.getPlayer());
            lastClimbLoc.remove(e.getPlayer());
            return;
        }

        if(lastClimbTime.containsKey(e.getPlayer())) {
            double timeDiff = (System.currentTimeMillis() - lastClimbTime.get(e.getPlayer())) / 50.0; //50ms = 1 tick
            double yDist = (e.getTo().getY() - lastClimbLoc.get(e.getPlayer()).getY()) * timeDiff;

            if(yDist > MAX_Y_DIFF_PER_TICK) {
                Notifier.notify(Check.MOVEMENT_FastLadder, e.getPlayer(), String.format("yDist: %.2f", yDist));
                Punishment.setBack(e.getPlayer(), e.getFrom());
            }
        }

        lastClimbTime.put(e.getPlayer(), System.currentTimeMillis());
        lastClimbLoc.put(e.getPlayer(), e.getTo());
    }

}
