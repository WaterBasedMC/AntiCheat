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

public class Punishment implements Listener {

    private static final double GRAVITY_CONST = 9.81;
    private static final HashMap<Player, Long> frozen = new HashMap<>();


    public static void pullDown(Player p) {

        Vector v = new Vector(0, 0, 0);
        long startTick = AntiCheat.tick;

        new Thread(() -> {
            while(!UtilCheat.isOnGround(p.getLocation(), 1)) {
                Bukkit.getScheduler().runTask(AntiCheat.instance, () -> {
                    Location to = p.getLocation().subtract(0, GRAVITY_CONST * ((AntiCheat.tick - startTick) / 20.0), 0);
                    while(to.getBlock().isSolid()) {
                        to.add(0, 0.1, 0);
                    }
                    p.teleport(to, PlayerTeleportEvent.TeleportCause.PLUGIN);
                });
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {}
            }
        }).start();
    }
    public static void freeze(Player player) {
        freeze(player, 10);
    }

    public static void freeze(Player player, long ticks) {
        frozen.put(player, AntiCheat.tick + ticks);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if(frozen.containsKey(e.getPlayer())) {
            if(AntiCheat.tick >= frozen.get(e.getPlayer())) {
                frozen.remove(e.getPlayer());
                return;
            }
            e.setCancelled(true);
        }
    }

}
