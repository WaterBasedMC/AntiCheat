package waterbased.anticheat.utils;

import jdk.jshell.execution.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;
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

    public static boolean isBeeingPunished(Player p) {
        return punishing.contains(p);
    }

    public static void pullDown(Player p) {
        if (isBeeingPunished(p)) return;

        punishing.add(p);

        Vector v = new Vector(0, 0, 0);
        long startTick = AntiCheat.tick;

        Location to = p.getLocation().clone();
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(AntiCheat.instance, new Runnable() {
            @Override
            public void run() {
                if(PlayerMovement.isOnGround(p) || p.isDead()) {
                    pullDownTask.get(p).cancel();
                    punishing.remove(p);
                    return;
                }
                double y = GRAVITY_CONST * ((AntiCheat.tick - startTick));
                if (y > 3.92) y = 3.92;

                for(double d = 0; d <= y; d += 0.1) {
                    if(UtilBlock.isInSolidBlock(to.clone().subtract(0, d, 0))) {
                        y = d-0.2;
                        pullDownTask.get(p).cancel();
                        punishing.remove(p);
                        break;
                    }
                }
                to.subtract(0, y, 0);
                p.teleport(to, PlayerTeleportEvent.TeleportCause.PLUGIN);
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
