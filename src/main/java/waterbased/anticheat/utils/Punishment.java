package waterbased.anticheat.utils;

import com.comphenix.protocol.PacketType;
import jdk.jshell.execution.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import waterbased.anticheat.AntiCheat;
import waterbased.anticheat.checks.movement.PlayerMovement;

import java.util.HashMap;
import java.util.HashSet;

public class Punishment implements Listener {

    private static final double GRAVITY_CONST = 0.08;
    private static final HashMap<Player, Long> frozen = new HashMap<>();

    private static final HashSet<Player> punishing = new HashSet<>();

    private static final HashMap<Player, BukkitTask> pullDownTask = new HashMap<>();
    private static final HashMap<Player, Location> pullDownLastLocation = new HashMap<>();

    public static boolean isBeeingPunished(Player p) {
        return punishing.contains(p);
    }

    public static void pullDown(Player p) {
        if (isBeeingPunished(p)) return;

        punishing.add(p);

        Vector v = new Vector(0, 0, 0);
        long startTick = AntiCheat.tick;
        Location start = p.getLocation().clone();
        Location highest = PlayerMovement.highestSinceGround.getOrDefault(p, start);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(AntiCheat.instance, new Runnable() {
            @Override
            public void run() {
                if(p.isDead() || PlayerMovement.isOnGround(p)) {
                    punishing.remove(p);
                    pullDownTask.remove(p).cancel();
                    pullDownLastLocation.remove(p);
                    return;
                }

                long time = AntiCheat.tick - startTick;
                double ySpeed = GRAVITY_CONST * time;
                if(ySpeed > 3.92) ySpeed = 3.92;

                Location from = pullDownLastLocation.getOrDefault(p, start);

                boolean done = false;
                double y = from.getY();

                while(y > from.getY() - ySpeed) {
                    if(!PlayerMovement.isOnGround(from.clone().subtract(0, from.getY() - y, 0), p.getBoundingBox(), 0.05)) {
                        y -= 0.05;
                    } else {
                        done = true;
                        break;
                    }
                }
                Location to = from.clone();
                to.setY(y);
                p.teleport(to);
                pullDownLastLocation.put(p, to);
                if(done) {
                    double fallDamage = highest.getY() - to.getY() - 3;
                    if(fallDamage >= 1) {
                        p.damage(fallDamage);
                    }
                    punishing.remove(p);
                    pullDownTask.remove(p).cancel();
                    pullDownLastLocation.remove(p);
                }
            }
        }, 0, 1);
        pullDownTask.put(p, task);
    }

    public static void freeze(Player player) {
        freeze(player, 10);
    }

    public static void freeze(Player player, long ticks) {
        frozen.put(player, AntiCheat.tick + ticks);
        punishing.add(player);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (frozen.containsKey(e.getPlayer())) {
            if (AntiCheat.tick >= frozen.get(e.getPlayer())) {
                frozen.remove(e.getPlayer());
                punishing.remove(e.getPlayer());
                return;
            }
            e.setCancelled(true);
        }
    }

}
