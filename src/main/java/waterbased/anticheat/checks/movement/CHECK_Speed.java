package waterbased.anticheat.checks.movement;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
import waterbased.anticheat.AntiCheat;
import waterbased.anticheat.events.PlayerPreciseMoveEvent;

public class CHECK_Speed implements Listener {

    public enum MovementDirection {
        FORWARD,
        FORWARD_STRAFE,
        FORWARD_JITTERY,
        BACKWARD,
        BACKWARD_STRAFE,
        BACKWARD_JITTERY,
        SIDEWAYS,
        UNKNOWN;
    }

    @EventHandler
    public void onMove(PlayerPreciseMoveEvent e) {

        if(!checkPacketDistance(e.getPlayer(), e.getFrom(), e.getTo())) {
            return;
        }

    }

    static double packetDistanceMax = 0;

    private boolean checkPacketDistance(Player player, Location from, Location to) {

        double xzDiff2 = Math.pow(to.getX() - from.getX(), 2) + Math.pow(to.getZ() - from.getZ(), 2);

        player.sendActionBar(Component.text(calcMovementDirection(player, from, to).toString()));

        return true;
    }

    private MovementDirection calcMovementDirection(Player player, Location from, Location to) {

        Vector direction = player.getEyeLocation().getDirection().setY(0).normalize();
        Vector movement = to.toVector().setY(0).subtract(from.toVector().setY(0)).normalize();

        double angle = direction.angle(movement);

        if(angle < 0.1) {
            return MovementDirection.FORWARD;
        }
        if(angle > 0.1 && angle < 0.65) {
            return MovementDirection.FORWARD_JITTERY;
        }
        if(angle > 0.65 && angle < 0.85) {
            return MovementDirection.FORWARD_STRAFE;
        }
        if(angle > 1.47 && angle < 1.67) {
            return MovementDirection.SIDEWAYS;
        }
        if(angle > 1.67 && angle < 2.25) {
            return MovementDirection.BACKWARD_JITTERY;
        }
        if(angle > 2.25 && angle < 2.45) {
            return MovementDirection.BACKWARD_STRAFE;
        }
        if(angle > Math.PI - 0.1 && angle < Math.PI + 0.1) {
            return MovementDirection.BACKWARD;
        }

        return MovementDirection.UNKNOWN;

    }



}
