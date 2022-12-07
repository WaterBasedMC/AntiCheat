package waterbased.anticheat.checks.movement;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import waterbased.anticheat.events.PlayerOnGroundChangeEvent;
import waterbased.anticheat.events.PlayerPreciseMoveEvent;
import waterbased.anticheat.checks.Check;
import waterbased.anticheat.utils.Notifier;
import waterbased.anticheat.utils.Punishment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CHECK_Flight implements Listener {

    public enum VerticalDirection {
        UP, DOWN, NONE

    }

    public static final int GRACE_NYM_COUNT = 6;
    public static final int GRACE_YSUM_COUNT = 3;
    public static final int GRACE_GROUND_MID_AIR_COUNT = 2;

    /*
        TODO: Count in JumpBoost, SlowFalling,

        Issues:
        - Walking on layer snow

     */
    private static final HashMap<Player, Integer> onGroundGrace = new HashMap<>();
    private static final HashMap<Player, Integer> sumGrace = new HashMap<>();
    private static final HashMap<Player, Integer> noYGrace = new HashMap<>();
    private static final HashMap<Player, List<Double>> vertMovements = new HashMap<>();
    private static final HashMap<Player, Double> lastYMove = new HashMap<>();
    private static final HashMap<Player, VerticalDirection> vertDirection = new HashMap<>();

    private static boolean checkMaxHeight(PlayerPreciseMoveEvent e) {
        Location lastSafe = PlayerMovement.getLastSafeLocation(e.getPlayer());
        if (e.getTo().getY() - lastSafe.getY() >= 1.5) { //TODO: Consider JumpBoost Effect/SlimeBlocks
            Notifier.notify(Check.MOVEMENT_Flight, e.getPlayer(), String.format("t: th, yd: %.2f", e.getTo().getY() - lastSafe.getY()));
            setBack(e.getPlayer());
            return true;
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
            if (vertMovements.containsKey(e.getPlayer())) {
                vertMovements.get(e.getPlayer()).clear();
            }
            lastYMove.remove(e.getPlayer());
            sumGrace.put(e.getPlayer(), 0);
            //noYGrace.put(e.getPlayer(), 0);
        }
    }

    private static boolean checkNoYMovement(PlayerPreciseMoveEvent e) {
        double yDif = Math.abs(e.getFrom().getY() - e.getTo().getY());
        yDif = Math.round(yDif * 1000) / 1000.0;
        if (yDif <= 0.1) { //Moving horizontally without falling
            noYGrace.put(e.getPlayer(), noYGrace.getOrDefault(e.getPlayer(), 0) + 1);
            if (noYGrace.get(e.getPlayer()) > GRACE_NYM_COUNT) {
                Notifier.notify(Check.MOVEMENT_Flight, e.getPlayer(), String.format("t: ny, yd: %.2f, g: %d", yDif, noYGrace.get(e.getPlayer())));
                setBack(e.getPlayer());
                return true;
            }
        }
        return false;
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
                            Notifier.notify(Check.MOVEMENT_Flight, e.getPlayer(), String.format("t: fna, s0: %.2f, s1: %.2f", lym, sum));
                            setBack(e.getPlayer());
                            return true;
                        }
                    }
                } else if (e.getFrom().getY() < e.getTo().getY()) { //Rising
                    if (lym <= sum) {
                        sumGrace.put(e.getPlayer(), sumGrace.getOrDefault(e.getPlayer(), 0) + 1);
                        if (sumGrace.get(e.getPlayer()) >= GRACE_YSUM_COUNT) {
                            Notifier.notify(Check.MOVEMENT_Flight, e.getPlayer(), String.format("t: nsd, s0: %.2f, s1: %.2f", lym, sum));
                            setBack(e.getPlayer());
                            return true;
                        }
                    }
                }
            }
            lastYMove.put(e.getPlayer(), sum);
        }

        return false;

    }

    private static void reset(Player player) {
        onGroundGrace.remove(player);
        sumGrace.remove(player);
        noYGrace.remove(player);
        vertMovements.remove(player);
        lastYMove.remove(player);
        vertDirection.remove(player);
    }

    private static void setBack(Player player) {
        Location lastSafe = PlayerMovement.getLastSafeLocation(player);
        if (player.getLocation().distance(lastSafe) <= 500) {
            Punishment.setBack(player, lastSafe);
        } else {
            Punishment.pullDown(player);
        }
    }

    @EventHandler
    public void onMove(PlayerPreciseMoveEvent e) {
        if (Punishment.isBeeingPunished(e.getPlayer())) {
            return;
        }

        if (e.getPlayer().getAllowFlight()
                || e.getPlayer().isInsideVehicle()
                || PlayerMovement.inLiquid(e.getPlayer())
                || PlayerMovement.isClimbing(e.getPlayer())
                || PlayerMovement.inWebs(e.getPlayer())
                || PlayerMovement.isOnGround(e.getPlayer())
                || e.getPlayer().isGliding()
                || e.getPlayer().isSleeping()) {
            reset(e.getPlayer());
            return;
        }

        checkMovementDirection(e); //Not a check only for clearing of graces.

        if (checkMaxHeight(e)) return;
        if (checkNoYMovement(e)) return;
        if (checkVerticalMovement(e)) return;

    }

    @EventHandler
    public void onGroundChange(PlayerOnGroundChangeEvent e) {
        if (Punishment.isBeeingPunished(e.getPlayer())) return;
        if (e.isOnGround()) {
            int grace = onGroundGrace.getOrDefault(e.getPlayer(), 0);
            grace++;
            if (grace >= GRACE_GROUND_MID_AIR_COUNT) {
                setBack(e.getPlayer());
                Notifier.notify(Check.MOVEMENT_Flight, e.getPlayer(), "t: OnGroundInAir");
                onGroundGrace.remove(e.getPlayer());
            } else {
                onGroundGrace.put(e.getPlayer(), grace);
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        reset(e.getPlayer());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        reset(e.getPlayer());
    }

}
