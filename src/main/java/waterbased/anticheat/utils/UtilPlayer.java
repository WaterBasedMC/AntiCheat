package waterbased.anticheat.utils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class UtilPlayer {

    public static void clear(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(false);
        player.setSprinting(false);
        player.setFoodLevel(20);
        player.setSaturation(3.0f);
        player.setExhaustion(0.0f);
        player.setMaxHealth(20.0);
        player.setHealth(player.getMaxHealth());
        player.setFireTicks(0);
        player.setFallDistance(0.0f);
        player.setLevel(0);
        player.setExp(0.0f);
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
        player.getInventory().clear();
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
        player.updateInventory();
        for (final PotionEffect potion : player.getActivePotionEffects()) {
            player.removePotionEffect(potion.getType());
        }
    }

    public static Location getEyeLocation(Player player) {
        final Location eye = player.getLocation();
        eye.setY(eye.getY() + player.getEyeHeight());
        return eye;
    }

    public static boolean isInWater(Player player) {
        final Material m = player.getLocation().getBlock().getType();
        return m == Material.WATER;
    }

    public static boolean isInLiquid(Player player) {
        return player.getLocation().getBlock().isLiquid();
    }

    public static boolean isOnClimbable(Player player, int blocks) {
        return isOnClimbable2(player);
    }

    private static boolean isOnClimbable2(Player player) {
        return player.getLocation().getBlock().getType().equals(Material.LADDER) || player.getLocation().getBlock().getType().equals(Material.VINE);
    }

    public static boolean isInAir(Player player) {
        for (Block block : UtilBlock.getSurrounding(player.getLocation().getBlock(), false)) {
            if (block.getType() == Material.AIR) {
                return true;
            }
        }
        return player.getLocation().getBlock().getType() == Material.AIR;
    }

    public static boolean isInAir(Location loc) {
        for (Block block : UtilBlock.getSurrounding(loc.getBlock(), false)) {
            if (block.getType() == Material.AIR) {
                return true;
            }
        }
        return loc.getBlock().getType() == Material.AIR;
    }

    public static boolean isPartiallyStuck(Player player) {
        if (player.getLocation().clone().getBlock() == null) {
            return false;
        }
        Block block = player.getLocation().clone().getBlock();
        if (UtilCheat.isSlab(block) || UtilCheat.isStair(block)) {
            return false;
        }
        if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isSolid()
                || player.getLocation().getBlock().getRelative(BlockFace.UP).getType().isSolid()) {
            return true;
        }
        if (player.getLocation().clone().add(0.0D, 1.0D, 0.0D).getBlock().getRelative(BlockFace.DOWN).getType()
                .isSolid()
                || player.getLocation().clone().add(0.0D, 1.0D, 0.0D).getBlock().getRelative(BlockFace.UP).getType()
                .isSolid()) {
            return true;
        }
        return block.getType().isSolid();
    }

    public static boolean isFullyStuck(Player player) {
        Block block1 = player.getLocation().clone().getBlock();
        Block block2 = player.getLocation().clone().add(0.0D, 1.0D, 0.0D).getBlock();
        if (block1.getType().isSolid() && block2.getType().isSolid()) {
            return true;
        }
        return block1.getRelative(BlockFace.DOWN).getType().isSolid()
                || block1.getLocation().getBlock().getRelative(BlockFace.UP).getType().isSolid()
                && block2.getRelative(BlockFace.DOWN).getType().isSolid()
                || block2.getLocation().getBlock().getRelative(BlockFace.UP).getType().isSolid();
    }

    public static boolean isOnGround(Player player) {
        return isOnGround2(player, 0.1);
//		if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR) {
//			return true;
//		}
//		Location a = player.getLocation().clone();
//		a.setY(a.getY() - 0.5);
//		if (a.getBlock().getType() != Material.AIR) {
//			return true;
//		}
//		a = player.getLocation().clone();
//		a.setY(a.getY() + 0.5);
//		return a.getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR
//				|| UtilCheat.isBlock(player.getLocation().getBlock().getRelative(BlockFace.DOWN),
//						new Material[] { Material.FENCE, Material.FENCE_GATE, Material.COBBLE_WALL, Material.LADDER });
    }

    public static boolean isOnGround(Player player, double d) {
        return isOnGround2(player, d);
    }

    private static boolean isOnGround2(Player player, double d) {

        Location loc = player.getLocation().clone();
        Block on = player.getLocation().clone().subtract(0.0, d, 0).getBlock();
        if (UtilBlock.isSolid(on)) {
            return true;
        }

        if (player.getLocation().getBlock().getType().equals(Material.SNOW) || on.getType().equals(Material.SNOW)) {
            return true;
        }

        Block b1 = loc.clone().add(0.3, -d, 0.0).getBlock();
        Block b2 = loc.clone().add(-0.3, -d, 0.0).getBlock();
        Block b3 = loc.clone().add(0.0, -d, 0.3).getBlock();
        Block b4 = loc.clone().add(0.0, -d, -0.3).getBlock();
        Block b5 = loc.clone().add(0.3, -d, 0.3).getBlock();
        Block b6 = loc.clone().add(0.3, -d, -0.3).getBlock();
        Block b7 = loc.clone().add(-0.3, -d, 0.3).getBlock();
        Block b8 = loc.clone().add(-0.3, -d, -0.3).getBlock();

        boolean c1 = UtilBlock.isSolid(b1) && !UtilBlock.isSolid(b1.getRelative(BlockFace.UP));
        boolean c2 = UtilBlock.isSolid(b2) && !UtilBlock.isSolid(b2.getRelative(BlockFace.UP));
        boolean c3 = UtilBlock.isSolid(b3) && !UtilBlock.isSolid(b3.getRelative(BlockFace.UP));
        boolean c4 = UtilBlock.isSolid(b4) && !UtilBlock.isSolid(b4.getRelative(BlockFace.UP));
        boolean c5 = UtilBlock.isSolid(b5) && !UtilBlock.isSolid(b5.getRelative(BlockFace.UP));
        boolean c6 = UtilBlock.isSolid(b6) && !UtilBlock.isSolid(b6.getRelative(BlockFace.UP));
        boolean c7 = UtilBlock.isSolid(b7) && !UtilBlock.isSolid(b7.getRelative(BlockFace.UP));
        boolean c8 = UtilBlock.isSolid(b8) && !UtilBlock.isSolid(b8.getRelative(BlockFace.UP));

        return c1 || c2 || c3 || c4 || c5 || c6 || c7 || c8;
    }

    public static List<Entity> getNearbyRidables(Location loc, double distance) {
        final List<Entity> entities = new ArrayList<Entity>();
        for (final Entity entity : new ArrayList<Entity>(loc.getWorld().getEntities())) {
            if (!entity.getType().equals(EntityType.HORSE)
                    && !entity.getType().equals(EntityType.BOAT)) {
                continue;
            }
            Bukkit.getServer()
                    .broadcastMessage(String.valueOf(entity.getLocation().distance(loc)));
            if (entity.getLocation().distance(loc) > distance) {
                continue;
            }
            entities.add(entity);
        }
        return entities;
    }

}