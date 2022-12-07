package waterbased.anticheat.checks.movement;

import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;
import waterbased.anticheat.AntiCheat;
import waterbased.anticheat.events.PlayerPreciseMoveEvent;
import waterbased.anticheat.checks.Check;
import waterbased.anticheat.utils.Notifier;
import waterbased.anticheat.utils.Punishment;

import java.util.HashMap;

public class CHECK_Speed implements Listener {

    public enum MovementDirection {
        FORWARD,
        FORWARD_STRAFE,
        FORWARD_JITTERY,
        BACKWARD,
        BACKWARD_STRAFE,
        BACKWARD_JITTERY,
        SIDEWAYS,
        UNKNOWN
    }

    private static final double MAX_PACKET_XZ = 0.381D;

    private static final HashMap<Player, Long> tickGrace = new HashMap<Player, Long>();

    @EventHandler
    public void onMove(PlayerPreciseMoveEvent e) {
        if (Punishment.isBeeingPunished(e.getPlayer())) {
            return;
        }
        if (e.getPlayer().getAllowFlight()) {
            return;
        }
        if (tickGrace.getOrDefault(e.getPlayer(), 0L) > AntiCheat.tick) {
        } else {
            tickGrace.remove(e.getPlayer());
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player p) {
            if (e.getDamager() instanceof Player dp) {
                int multi = dp.getInventory().getItemInMainHand().getEnchantments().getOrDefault(Enchantment.KNOCKBACK, 0);
                tickGrace.put(p, AntiCheat.tick + 5L + (multi * 2L));
            } else if (e.getDamager() instanceof Projectile projectile) {
                tickGrace.put(p, AntiCheat.tick + 2L);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTeleport(PlayerTeleportEvent e) {
        tickGrace.put(e.getPlayer(), AntiCheat.tick + 2);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRespawn(PlayerRespawnEvent e) {
        tickGrace.put(e.getPlayer(), AntiCheat.tick + 2);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMount(EntityMountEvent e) {
        if (e.getEntity() instanceof Player p) {
            tickGrace.put(p, AntiCheat.tick + 5);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMount(EntityDismountEvent e) {
        if (e.getEntity() instanceof Player p) {
            tickGrace.put(p, AntiCheat.tick + 5);
        }
    }

    private boolean checkPacketDistance(Player player, Location from, Location to) {
        double xzDiff2 = Math.pow(to.getX() - from.getX(), 2) + Math.pow(to.getZ() - from.getZ(), 2);
        if (xzDiff2 > MAX_PACKET_XZ) {
            Notifier.notify(Check.MOVEMENT_Speed, player, String.format("t: mpd, d: %.2f", xzDiff2));
            Punishment.setBack(player, from, true);
            return false;
        }

        return true;
    }

    private MovementDirection calcMovementDirection(Player player, Location from, Location to) {

        Vector direction = player.getEyeLocation().getDirection().setY(0).normalize();
        Vector movement = to.toVector().setY(0).subtract(from.toVector().setY(0)).normalize();

        double angle = direction.angle(movement);

        if (angle < 0.1) {
            return MovementDirection.FORWARD;
        }
        if (angle > 0.1 && angle < 0.65) {
            return MovementDirection.FORWARD_JITTERY;
        }
        if (angle > 0.65 && angle < 0.85) {
            return MovementDirection.FORWARD_STRAFE;
        }
        if (angle > 1.47 && angle < 1.67) {
            return MovementDirection.SIDEWAYS;
        }
        if (angle > 1.67 && angle < 2.25) {
            return MovementDirection.BACKWARD_JITTERY;
        }
        if (angle > 2.25 && angle < 2.45) {
            return MovementDirection.BACKWARD_STRAFE;
        }
        if (angle > Math.PI - 0.1 && angle < Math.PI + 0.1) {
            return MovementDirection.BACKWARD;
        }

        return MovementDirection.UNKNOWN;

    }

}
