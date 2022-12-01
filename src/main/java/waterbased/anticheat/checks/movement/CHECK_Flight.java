package waterbased.anticheat.checks.movement;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import waterbased.anticheat.AntiCheat;
import waterbased.anticheat.events.PlayerOnGroundChangeEvent;
import waterbased.anticheat.events.PlayerPreciseMoveEvent;
import waterbased.anticheat.utils.Notifier;
import waterbased.anticheat.utils.Punishment;
import waterbased.anticheat.utils.UtilCheat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

public class CHECK_Flight implements Listener {

    public enum VerticalDirection {
        UP,
        DOWN,
        NONE;
    }

    private static HashMap<Player, Location> lastGround = new HashMap<>();
    private static final HashMap<Player, Integer> onGroundGrace = new HashMap<>();
    private static final HashMap<Player, Integer> sumGrace = new HashMap<>();
    private static final HashMap<Player, List<Double>> vertMovements = new HashMap<>();
    private static final HashMap<Player, Double> lastYMove = new HashMap<>();
    private static final HashMap<Player, VerticalDirection> vertDirection = new HashMap<>();

    @EventHandler
    public void onMove(PlayerPreciseMoveEvent e) {
        if(Punishment.isBeeingPunished(e.getPlayer())) return;

        if (UtilCheat.isOnGround(e.getTo(), 1) || e.getPlayer().getAllowFlight() || !lastGround.containsKey(e.getPlayer())) {
            lastGround.put(e.getPlayer(), e.getTo());
            onGroundGrace.put(e.getPlayer(), 0);
            if(vertMovements.containsKey(e.getPlayer()))
                vertMovements.get(e.getPlayer()).clear();
            return;
        }
        if (e.getTo().getY() - lastGround.get(e.getPlayer()).getY() > 2.5) { //TODO: Consider JumpBoost Effect
            e.getPlayer().teleport(lastGround.get(e.getPlayer()));
            Punishment.freeze(e.getPlayer());
        }

        if(!vertMovements.containsKey(e.getPlayer())) vertMovements.put(e.getPlayer(), new ArrayList<>());
        vertMovements.get(e.getPlayer()).add(Math.abs(e.getFrom().getY() - e.getTo().getY()));
        if(vertMovements.get(e.getPlayer()).size() > 15) vertMovements.get(e.getPlayer()).remove(0);

        if(!vertDirection.containsKey(e.getPlayer())) vertDirection.put(e.getPlayer(), VerticalDirection.NONE);

        boolean changedDirection = false;
        if(e.getFrom().getY() > e.getTo().getY() && vertDirection.get(e.getPlayer()) != VerticalDirection.DOWN) {
            vertDirection.put(e.getPlayer(), VerticalDirection.DOWN);
            changedDirection = true;
        } else if(e.getFrom().getY() < e.getTo().getY() && vertDirection.get(e.getPlayer()) != VerticalDirection.UP) {
            vertDirection.put(e.getPlayer(), VerticalDirection.UP);
            changedDirection = true;
        } else if(e.getFrom().getY() == e.getTo().getY() && vertDirection.get(e.getPlayer()) != VerticalDirection.NONE) {
            vertDirection.put(e.getPlayer(), VerticalDirection.NONE);
            changedDirection = true;
        }

        if(changedDirection) {
            vertMovements.get(e.getPlayer()).clear();
            lastYMove.remove(e.getPlayer());
            sumGrace.put(e.getPlayer(), 0);
        }

        List<Double> ys = vertMovements.get(e.getPlayer());
        if(ys.size() >= 5) {
            double sum = 0;
            StringBuilder s = new StringBuilder();
            for (Double d : ys) {
                sum += d;
                s.append(", ").append(String.format("%.6f", d));
            }
            sum /= ys.size();

            if (lastYMove.containsKey(e.getPlayer())) {
                double lym = lastYMove.get(e.getPlayer());
                if (e.getFrom().getY() > e.getTo().getY()) { //Falling
                    if (lym >= sum) { //Not accelerating!
                        sumGrace.put(e.getPlayer(), sumGrace.getOrDefault(e.getPlayer(), 0) + 1);
                        if(sumGrace.get(e.getPlayer()) >= 2) {
                            Punishment.pullDown(e.getPlayer());
                            Notifier.notify(Notifier.Check.MOVEMENT_Flight, e.getPlayer(), String.format("t: fna, s0: %.2f, s1: %.2f", lym, sum));
                        }
                    }
                } else if (e.getFrom().getY() < e.getTo().getY()) { //Rising
                    if (lym <= sum) {
                        sumGrace.put(e.getPlayer(), sumGrace.getOrDefault(e.getPlayer(), 0) + 1);
                        if(sumGrace.get(e.getPlayer()) >= 2) {
                            Punishment.pullDown(e.getPlayer());
                            Notifier.notify(Notifier.Check.MOVEMENT_Flight, e.getPlayer(), String.format("t: nsd, s0: %.2f, s1: %.2f", lym, sum));
                        }
                    }
                }
            }
            lastYMove.put(e.getPlayer(), sum);
        }

        e.getPlayer().sendActionBar(Component.text(vertDirection.get(e.getPlayer()).toString()));

    }

    @EventHandler
    public void onGroundChange(PlayerOnGroundChangeEvent e) {
        if(Punishment.isBeeingPunished(e.getPlayer())) return;
        if (e.isOnGround() && !UtilCheat.isOnGround(e.getPlayer().getLocation())) {
            int grace = onGroundGrace.getOrDefault(e.getPlayer(), 0);
            grace++;
            if (grace >= 3) {
                Punishment.pullDown(e.getPlayer());
                Notifier.notify(Notifier.Check.MOVEMENT_Flight, e.getPlayer(), "t: OnGroundInAir");
                onGroundGrace.remove(e.getPlayer());
            } else {
                onGroundGrace.put(e.getPlayer(), grace);
            }
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        if(e.isSneaking()) {
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 60, 1, true,  false));
        } else {
            e.getPlayer().removePotionEffect(PotionEffectType.GLOWING);
        }
    }


}
