package waterbased.anticheat.checks.other;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitTask;
import waterbased.anticheat.events.PlayerPreciseMoveEvent;
import waterbased.anticheat.utils.Notifier;
import waterbased.anticheat.utils.Punishment;
import waterbased.anticheat.utils.UtilCheat;

import java.util.HashMap;
import java.util.HashSet;


public class CHECK_NoFall implements Listener {

    private class DamageDue {
        public final double damage;
        public final BukkitTask task;
        public DamageDue(double damage, BukkitTask task) {
            this.damage = damage;
            this.task = task;
        }
    }

    private class TookDamage {
        public final double damage;
        public final long tick;
        public final EntityDamageEvent.DamageCause cause;
        public TookDamage(double damage, long tick, EntityDamageEvent.DamageCause cause) {
            this.damage = damage;
            this.tick = tick;
            this.cause = cause;
        }
    }

    private final HashMap<Player, Location> highest = new HashMap<>();
    private final HashMap<Player, Location> lastOnGround = new HashMap<>();
    private final HashSet<Player> falling = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerPreciseMoveEvent e) {
        boolean onGround = UtilCheat.isOnGround(e.getTo(), 1);
        if (onGround) {
            highest.put(e.getPlayer(), e.getTo());
            lastOnGround.put(e.getPlayer(), e.getTo());
            falling.remove(e.getPlayer());
        } else {
            if(highest.getOrDefault(e.getPlayer(), e.getTo()).getY() < e.getTo().getY()) {
                highest.put(e.getPlayer(), e.getTo());
                falling.remove(e.getPlayer());
            } else {
                falling.add(e.getPlayer());
            }
        }

    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if(e.getEntity() instanceof Player p) {
            if(e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                double yDiff = highest.get(p).getY() - p.getLocation().getY();
                double sd = yDiff - 3;

                Block landingBlock = p.getLocation().getBlock().getRelative(BlockFace.DOWN);

                if(landingBlock.getType() == Material.HAY_BLOCK) {
                    sd = sd * 0.2;
                } else if(landingBlock.getType().toString().endsWith("_BED")) {
                    sd = sd * 0.5;
                }

                if(yDiff > 3) {
                    if(e.getDamage() / sd < 0.9) { //Not enough damage
                        Notifier.notify(Notifier.Check.OTHER_NoFall, p, String.format("t: %s, y: %.2f d: %.2f sd: %.2f", "lessDmg", yDiff, e.getDamage(), sd));
                        e.setDamage(yDiff-3);
                        Punishment.freeze(p);
                    }
                }
            }
        }
    }


}
