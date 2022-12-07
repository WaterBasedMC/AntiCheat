package waterbased.anticheat.checks.movement;

import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import waterbased.anticheat.checks.Check;
import waterbased.anticheat.utils.Notifier;

public class CHECK_VehicleFlight implements Listener {

    private static boolean checkVehicleYMove(Player player, Location from, Location to) {
        Vehicle vehicle = (Vehicle) player.getVehicle();
        if (!player.isInsideVehicle() || vehicle == null) return true;

        double yMovement = to.getY() - from.getY();

        if (yMovement <= 0) {
            return true;
        }

        if (vehicle instanceof AbstractHorse) {
            return true;
        } else {
            //Check Minecart on Rails
            if (vehicle instanceof Minecart m && m.getLocation().getBlock().getType().toString().endsWith("_RAIL")) {
                return true;
            }
            if (vehicle instanceof Boat b) {
                if (b.isInWater()) {
                    return true;
                }
            }
        }
        Notifier.notify(Check.MOVEMENT_VehicleFlight, player, "t: ym, y: %.2f, v: %s".formatted(yMovement, vehicle.getType().toString()));
        vehicle.removePassenger(player);
        return false;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getPlayer().isInsideVehicle()) {
            if (!checkVehicleYMove(e.getPlayer(), e.getFrom(), e.getTo())) return;
        }
    }

}
