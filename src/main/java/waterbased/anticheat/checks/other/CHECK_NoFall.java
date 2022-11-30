package waterbased.anticheat.checks.other;

import net.kyori.adventure.text.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;
import waterbased.anticheat.AntiCheat;
import waterbased.anticheat.utils.Notifier;
import waterbased.anticheat.utils.UtilCheat;

import java.util.HashMap;
import java.util.UUID;


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

    private final HashMap<UUID, Location> highest = new HashMap<>();
    private final HashMap<UUID, Boolean> lastMovementWasOnGround = new HashMap<>();
    private final HashMap<UUID, TookDamage> tookFallDamage = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent e) {

        if(UtilCheat.isOnGround(e.getPlayer().getLocation(), 1);


        boolean isOnGround = e.getPlayer().getLocation().add(0, -0.1, 0).getBlock().getType() != Material.AIR;
        boolean wasOnGround = this.lastMovementWasOnGround.getOrDefault(e.getPlayer().getUniqueId(), false);

        if (isOnGround) {
            Location up = null;
            if(!wasOnGround && (up = lastLocationOnGround.getOrDefault(e.getPlayer().getUniqueId(), null)) != null) {
                lastLocationOnGround.put(e.getPlayer().getUniqueId(), e.getPlayer().getLocation());
                lastMovementWasOnGround.put(e.getPlayer().getUniqueId(), true);
                double yDiff = up.getY() - e.getTo().getY();
                if(yDiff > 3) {
                    if(tookFallDamage.containsKey(e.getPlayer().getUniqueId())) {
                        TookDamage tookDamage = tookFallDamage.get(e.getPlayer().getUniqueId());
                        if(tookDamage.tick > AntiCheat.tick - 20) { //Falldamage timing ok.
                            if(tookDamage.damage / (yDiff - 3) < 0.85) {
                                e.getPlayer().damage((yDiff - 3) - tookDamage.damage);
                                Notifier.notify(Notifier.Check.OTHER_NoFall,
                                        e.getPlayer(),
                                        String.format("yd: %.2f, td: %.2f, d: %.2f", yDiff, tookDamage.damage, (yDiff - 3) - tookDamage.damage));
                            }
                            return;
                        }
                    }
                    Bukkit.getScheduler().runTaskLater(AntiCheat.instance, () -> {
                        if(tookFallDamage.containsKey(e.getPlayer().getUniqueId())) {
                            TookDamage tookD = tookFallDamage.get(e.getPlayer().getUniqueId());
                            if(tookD.tick < AntiCheat.tick - 20) {
                                tookFallDamage.remove(e.getPlayer().getUniqueId());
                                e.getPlayer().damage(yDiff - 3);
                                Notifier.notify(Notifier.Check.OTHER_NoFall,
                                        e.getPlayer(),
                                        String.format("yd: %.2f, td: %.2f, d: %.2f", yDiff, 0.0f, (yDiff - 3)));

                            } else {
                                if(tookD.damage / (yDiff - 3) < 0.85) {
                                    e.getPlayer().damage((yDiff - 3) - tookD.damage);
                                    Notifier.notify(Notifier.Check.OTHER_NoFall,
                                            e.getPlayer(),
                                            String.format("yd: %.2f, td: %.2f, d: %.2f", yDiff, tookD.damage, (yDiff - 3) - tookD.damage));
                                }
                                tookFallDamage.remove(e.getPlayer().getUniqueId());
                            }
                        } else {
                            e.getPlayer().damage(yDiff - 3);
                            Notifier.notify(Notifier.Check.OTHER_NoFall,
                                    e.getPlayer(),
                                    String.format("yd: %.2f, td: %.2f, d: %.2f", yDiff, 0.0f, (yDiff - 3)));
                        }
                    }, 10);
                }
            }
            lastLocationOnGround.put(e.getPlayer().getUniqueId(), e.getPlayer().getLocation());
        }
        lastMovementWasOnGround.put(e.getPlayer().getUniqueId(), isOnGround);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if(e.getEntity() instanceof Player p) {
            if(e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                tookFallDamage.put(p.getUniqueId(), new TookDamage(e.getDamage(), AntiCheat.tick, e.getCause()));
            }
        }
    }


}
