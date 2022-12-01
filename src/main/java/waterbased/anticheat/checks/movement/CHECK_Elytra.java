package waterbased.anticheat.checks.movement;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.inventory.ItemStack;
import waterbased.anticheat.utils.Notifier;

public class CHECK_Elytra implements Listener {

    @EventHandler
    public void onToggleGlide(EntityToggleGlideEvent e) {
        if (e.getEntity() instanceof Player p) {
            if (e.isGliding() && (p.getInventory().getChestplate() == null || p.getInventory().getChestplate().getType() != Material.ELYTRA)) {
                e.setCancelled(true);
                ItemStack chestplate = p.getInventory().getChestplate();
                Notifier.notify(Notifier.Check.MOVEMENT_ElytraFlight, p, String.format("t: wi, ci: %s", chestplate == null ? "none" : chestplate.getType().toString()));
            }
        }
    }

}
