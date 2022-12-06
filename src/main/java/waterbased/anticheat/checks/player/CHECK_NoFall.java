package waterbased.anticheat.checks.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import waterbased.anticheat.AntiCheat;
import waterbased.anticheat.events.PlayerOnGroundChangeEvent;
import waterbased.anticheat.events.PlayerPreciseMoveEvent;
import waterbased.anticheat.utils.Notifier;

import java.util.HashMap;

public class CHECK_NoFall implements Listener {

    public static final int POSITION_PACKET_GRACE_NO_DAMAGE_COUNT = 3;

    private final HashMap<Player, Integer> noDamageGrace = new HashMap<>();
    private final HashMap<Player, Double> shouldDamage = new HashMap<>();

    private final HashMap<Player, Long> lastFallDamage = new HashMap<>();

    @EventHandler
    public void onGroundChange(PlayerOnGroundChangeEvent e) {
        if (e.isOnGround()) {
            if (e.getFallDistance() - 3.0 >= 1) {
                if(lastFallDamage.getOrDefault(e.getPlayer(), 0L) + 2 > AntiCheat.tick) { //Took fallDamage in last 2 ticks
                    return;
                }
                noDamageGrace.put(e.getPlayer(), POSITION_PACKET_GRACE_NO_DAMAGE_COUNT);
                shouldDamage.put(e.getPlayer(), e.getFallDistance() - 3.0);
                //TODO: Check block of landing (Wheat, Bed, Slime, etc.)
            }
        }
    }

    @EventHandler
    public void onPreciseMove(PlayerPreciseMoveEvent e) {

        if (noDamageGrace.containsKey(e.getPlayer())) {
            noDamageGrace.put(e.getPlayer(), noDamageGrace.get(e.getPlayer()) - 1);
            if (noDamageGrace.get(e.getPlayer()) <= 0) {
                e.getPlayer().damage(shouldDamage.get(e.getPlayer()));
                Notifier.notify(Notifier.Check.PLAYER_NoFall, e.getPlayer(),
                        "t: %s, g: %d, d: %.2f".formatted("nd",
                                POSITION_PACKET_GRACE_NO_DAMAGE_COUNT,
                                shouldDamage.get(e.getPlayer())));
                noDamageGrace.remove(e.getPlayer());
                shouldDamage.remove(e.getPlayer());
            }
        }

    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p) {
            if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                lastFallDamage.put(p, AntiCheat.tick);
                noDamageGrace.remove(p);
                shouldDamage.remove(p);
            }
        }
    }

}
