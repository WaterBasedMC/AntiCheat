package waterbased.anticheat.checks.movement;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import waterbased.anticheat.AntiCheat;
import waterbased.anticheat.events.PlayerPreciseMoveEvent;
import waterbased.anticheat.checks.Check;
import waterbased.anticheat.utils.Notifier;
import waterbased.anticheat.utils.Punishment;
import waterbased.anticheat.utils.UtilBlock;

import java.util.HashMap;

public class CHECK_FastLadder implements Listener {

    public static final double TOLERANCE_LADDER_Y = 0.1;

    private static final HashMap<Player, Double> climbYLevel = new HashMap<>();
    private static final HashMap<Player, Long> climbTick = new HashMap<>();
    private static final HashMap<Player, Integer> climbGrace = new HashMap<>();

    @EventHandler
    public void onMove(PlayerPreciseMoveEvent e) {

        if (UtilBlock.isClimbableBlock(e.getPlayer().getLocation().getBlock())) {
            if (climbTick.getOrDefault(e.getPlayer(), 0L) < AntiCheat.tick) {
                if (climbYLevel.containsKey(e.getPlayer())) {
                    double yDiff = e.getPlayer().getLocation().getY() - climbYLevel.get(e.getPlayer());
                    if (yDiff > 0.4 + TOLERANCE_LADDER_Y) {
                        climbGrace.put(e.getPlayer(), climbGrace.getOrDefault(e.getPlayer(), 0) + 1);
                        if (climbGrace.get(e.getPlayer()) > 3) {
                            Notifier.notify(Check.MOVEMENT_FastLadder, e.getPlayer(), String.format("t: yd, yd: %.2f", yDiff));
                            Punishment.setBack(e.getPlayer(), e.getFrom(), true);
                            climbGrace.put(e.getPlayer(), 0);
                        }
                    }
                }
                climbTick.put(e.getPlayer(), AntiCheat.tick + 1);
                climbYLevel.put(e.getPlayer(), e.getPlayer().getLocation().getY());
            }
        }

    }

}
