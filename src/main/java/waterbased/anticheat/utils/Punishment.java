package waterbased.anticheat.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import waterbased.anticheat.AntiCheat;

import java.util.HashMap;
import java.util.HashSet;

public class Punishment implements Listener {

    private static final double GRAVITY_CONST = 0.08;
    private static final HashMap<Player, Long> frozen = new HashMap<>();

    private static final HashSet<Player> punishing = new HashSet<>();

    public static boolean isBeeingPunished(Player p) {
        return punishing.contains(p);
    }

    public static void pullDown(Player p) {
        if (isBeeingPunished(p)) return;

        punishing.add(p);

        Vector v = new Vector(0, 0, 0);
        long startTick = AntiCheat.tick;

        new Thread(() -> {
            Location to = p.getLocation().clone();
            while (!UtilCheat.isOnGround(p.getLocation()) && !p.isDead()) {
                Bukkit.getScheduler().runTask(AntiCheat.instance, () -> {
                    double y = GRAVITY_CONST * ((AntiCheat.tick - startTick));
                    if (y > 3.92) y = 3.92;

                    to.subtract(0, y, 0);
                    while (to.getBlock().isSolid()) {
                        to.add(0, 0.1, 0);
                    }
                    p.teleport(to, PlayerTeleportEvent.TeleportCause.PLUGIN);
                });
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    punishing.remove(p);
                }
            }
            punishing.remove(p);
        }).start();
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
