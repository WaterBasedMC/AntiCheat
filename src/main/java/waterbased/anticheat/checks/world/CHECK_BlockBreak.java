package waterbased.anticheat.checks.world;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.util.RayTraceResult;
import waterbased.anticheat.checks.Check;
import waterbased.anticheat.utils.Notifier;
import waterbased.anticheat.utils.Punishment;

public class CHECK_BlockBreak implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return; //Do not check in creative mode

        RayTraceResult result = e.getPlayer().rayTraceBlocks(5.0f);
        if (result == null) {
            e.setCancelled(true);
            Punishment.setBack(e.getPlayer(), e.getPlayer().getLocation(), false);
            Notifier.notify(Check.WORLD_BlockBreak, e.getPlayer(), "t: rt, r: none");
            return;
        }
        if (result.getHitBlock() == null) {
            e.setCancelled(true);
            Punishment.setBack(e.getPlayer(), e.getPlayer().getLocation(), false);
            Notifier.notify(Check.WORLD_BlockBreak, e.getPlayer(),
                    String.format("t: rt, r: %s, l: [%.2f, %.2f, %.2f]", "nblock", result.getHitPosition().getX(), result.getHitPosition().getY(), result.getHitPosition().getZ()));
            return;
        }
        if (!result.getHitBlock().getLocation().toBlockLocation().equals(e.getBlock().getLocation().toBlockLocation())) {
            e.setCancelled(true);
            Punishment.setBack(e.getPlayer(), e.getPlayer().getLocation(), false);
            Notifier.notify(Check.WORLD_BlockBreak, e.getPlayer(),
                    String.format("t: rt, r: %s, l: [%.2f, %.2f, %.2f], t1: %s, t2: %s", "nequ", result.getHitPosition().getX(), result.getHitPosition().getY(), result.getHitPosition().getZ(), e.getBlock().getType(), result.getHitBlock().getType()));
        }

    }

}
