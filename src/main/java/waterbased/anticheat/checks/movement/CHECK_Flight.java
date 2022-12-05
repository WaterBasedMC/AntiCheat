package waterbased.anticheat.checks.movement;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import waterbased.anticheat.events.PlayerOnGroundChangeEvent;
import waterbased.anticheat.events.PlayerPreciseMoveEvent;
import waterbased.anticheat.utils.Notifier;
import waterbased.anticheat.utils.Punishment;
import waterbased.anticheat.utils.UtilCheat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CHECK_Flight implements Listener {

    public static final int GRACE_NYM_COUNT = 8;
    public static final int GRACE_YSUM_COUNT = 3;
    public static final int GRACE_GROUND_MID_AIR_COUNT = 3;

    /*
        TODO: Count in JumpBoost, SlowFalling,

        Issues:
        - Walking on layer snow

     */

    public enum VerticalDirection {
        UP, DOWN, NONE

        }
    private static final HashMap<Player, Location> lastGround = new HashMap<>();
    private static final HashMap<Player, Integer> onGroundGrace = new HashMap<>();
    private static final HashMap<Player, Integer> sumGrace = new HashMap<>();
    private static final HashMap<Player, Integer> noYGrace = new HashMap<>();
    private static final HashMap<Player, List<Double>> vertMovements = new HashMap<>();
    private static final HashMap<Player, Double> lastYMove = new HashMap<>();
    private static final HashMap<Player, VerticalDirection> vertDirection = new HashMap<>();

    @EventHandler
    public void onMove(PlayerPreciseMoveEvent e) {
        if (Punishment.isBeeingPunished(e.getPlayer())) {
            return;
        }

        if (UtilCheat.isOnGround(e.getTo())
                || e.getPlayer().getAllowFlight()
                || !lastGround.containsKey(e.getPlayer())
                || e.getPlayer().getVehicle() != null) {
            lastGround.put(e.getPlayer(), e.getTo());
            onGroundGrace.put(e.getPlayer(), 0);
            noYGrace.put(e.getPlayer(), 0);
            sumGrace.put(e.getPlayer(), 0);
            if (vertMovements.containsKey(e.getPlayer())) vertMovements.get(e.getPlayer()).clear();
            return;
        }
        if (e.getPlayer().isGliding()) {
            return;
        }

        checkMovementDirection(e); //Not a check only for clearing of graces.

        if (checkMaxHeight(e)) return;
        if (checkNoYMovement(e)) return;
        if(checkVerticalMovement(e)) return;


    }

    private static boolean checkVerticalMovement(PlayerPreciseMoveEvent e) {
        if (!vertMovements.containsKey(e.getPlayer())) vertMovements.put(e.getPlayer(), new ArrayList<>());
        vertMovements.get(e.getPlayer()).add(Math.abs(e.getFrom().getY() - e.getTo().getY()));
        if (vertMovements.get(e.getPlayer()).size() > 15) vertMovements.get(e.getPlayer()).remove(0);

        List<Double> ys = vertMovements.get(e.getPlayer());
        if (ys.size() >= 5) {
            double sum = 0;
            for (Double d : ys) {
                sum += d;
            }
            sum /= ys.size();

            if (lastYMove.containsKey(e.getPlayer())) {
                double lym = lastYMove.get(e.getPlayer());
                if (e.getFrom().getY() > e.getTo().getY()) { //Falling
                    if (lym >= sum) { //Not accelerating!
                        sumGrace.put(e.getPlayer(), sumGrace.getOrDefault(e.getPlayer(), 0) + 1);
                        if (sumGrace.get(e.getPlayer()) >= GRACE_YSUM_COUNT) {
                            Punishment.pullDown(e.getPlayer());
                            Notifier.notify(Notifier.Check.MOVEMENT_Flight, e.getPlayer(), String.format("t: fna, s0: %.2f, s1: %.2f", lym, sum));
                            return true;
                        }
                    }
                } else if (e.getFrom().getY() < e.getTo().getY()) { //Rising
                    if (lym <= sum) {
                        sumGrace.put(e.getPlayer(), sumGrace.getOrDefault(e.getPlayer(), 0) + 1);
                        if (sumGrace.get(e.getPlayer()) >= GRACE_YSUM_COUNT) {
                            Punishment.pullDown(e.getPlayer());
                            Notifier.notify(Notifier.Check.MOVEMENT_Flight, e.getPlayer(), String.format("t: nsd, s0: %.2f, s1: %.2f", lym, sum));
                            return true;
                        }
                    }
                }
            }
            lastYMove.put(e.getPlayer(), sum);
        }

        return false;

    }

    private static void checkMovementDirection(PlayerPreciseMoveEvent e) {
        if (!vertDirection.containsKey(e.getPlayer())) vertDirection.put(e.getPlayer(), VerticalDirection.NONE);
        boolean changedDirection = false;
        if (e.getFrom().getY() > e.getTo().getY() && vertDirection.get(e.getPlayer()) != VerticalDirection.DOWN) {
            vertDirection.put(e.getPlayer(), VerticalDirection.DOWN);
            changedDirection = true;
        } else if (e.getFrom().getY() < e.getTo().getY() && vertDirection.get(e.getPlayer()) != VerticalDirection.UP) {
            vertDirection.put(e.getPlayer(), VerticalDirection.UP);
            changedDirection = true;
        } else if (e.getFrom().getY() == e.getTo().getY() && vertDirection.get(e.getPlayer()) != VerticalDirection.NONE) {
            vertDirection.put(e.getPlayer(), VerticalDirection.NONE);
            changedDirection = true;
        }

        if (changedDirection) {
            if(vertMovements.containsKey(e.getPlayer())) {
                vertMovements.get(e.getPlayer()).clear();
            }
            lastYMove.remove(e.getPlayer());
            sumGrace.put(e.getPlayer(), 0);
            //noYGrace.put(e.getPlayer(), 0);
        }
    }

    private static boolean checkMaxHeight(PlayerPreciseMoveEvent e) {
        if (e.getTo().getY() - lastGround.get(e.getPlayer()).getY() > 2.5) { //TODO: Consider JumpBoost Effect/SlimeBlocks
            e.getPlayer().teleport(lastGround.get(e.getPlayer()));
            Punishment.pullDown(e.getPlayer());
            Notifier.notify(Notifier.Check.MOVEMENT_Flight, e.getPlayer(), String.format("t: th, yd: %.2f", e.getTo().getY() - lastGround.get(e.getPlayer()).getY()));
            return true;
        }
        return false;
    }

    private static boolean checkNoYMovement(PlayerPreciseMoveEvent e) {
        if(e.getPlayer().getLocation().getBlock().getType() != Material.SNOW) {
            double yDif = Math.abs(e.getFrom().getY() - e.getTo().getY());
            yDif = Math.round(yDif * 1000) / 1000.0;
            if (yDif <= 0.1) { //Moving horizontally without falling
                noYGrace.put(e.getPlayer(), noYGrace.getOrDefault(e.getPlayer(), 0) + 1);
                if (noYGrace.get(e.getPlayer()) >= GRACE_NYM_COUNT) {
                    Punishment.pullDown(e.getPlayer());
                    Notifier.notify(Notifier.Check.MOVEMENT_Flight, e.getPlayer(), String.format("t: ny, yd: %.2f, g: %d", yDif, noYGrace.get(e.getPlayer())));
                    return true;
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onGroundChange(PlayerOnGroundChangeEvent e) {
        if (Punishment.isBeeingPunished(e.getPlayer())) return;
        if (e.isOnGround() && !UtilCheat.isOnGround(e.getPlayer().getLocation())) {
            int grace = onGroundGrace.getOrDefault(e.getPlayer(), 0);
            grace++;
            if (grace >= GRACE_GROUND_MID_AIR_COUNT) {
                Punishment.pullDown(e.getPlayer());
                Notifier.notify(Notifier.Check.MOVEMENT_Flight, e.getPlayer(), "t: OnGroundInAir");
                onGroundGrace.remove(e.getPlayer());
            } else {
                onGroundGrace.put(e.getPlayer(), grace);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTeleport(PlayerTeleportEvent e) {
        lastGround.put(e.getPlayer(), e.getTo());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRespawn(PlayerRespawnEvent e) {
        lastGround.put(e.getPlayer(), e.getRespawnLocation());
        onGroundGrace.put(e.getPlayer(), 0);
        noYGrace.put(e.getPlayer(), 0);
        sumGrace.put(e.getPlayer(), 0);
    }

}
