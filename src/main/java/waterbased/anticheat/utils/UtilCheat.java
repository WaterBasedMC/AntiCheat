package waterbased.anticheat.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Gate;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UtilCheat {

    public static boolean blocksNear(Player player) {
        return blocksNear(player.getLocation());
    }

    public static boolean blocksNear(final Location loc) {
        boolean nearBlocks = false;
        for (Block block : UtilBlock.getSurrounding(loc.getBlock(), true)) {
            if (block.getType() != Material.AIR) {
                nearBlocks = true;
                break;
            }
        }
        for (final Block block : UtilBlock.getSurrounding(loc.getBlock(), false)) {
            if (block.getType() != Material.AIR) {
                nearBlocks = true;
                break;
            }
        }
        loc.setY(loc.getY() - 0.5);
        if (loc.getBlock().getType() != Material.AIR) {
            nearBlocks = true;
        }
        List<Material> fences = Arrays.stream(Material.values()).filter(m -> m.toString().contains("FENCE") || m.toString().contains("_FENCE_GATE") || m == Material.LADDER).toList();
        if (isBlock(loc.getBlock().getRelative(BlockFace.DOWN), fences.toArray(new Material[0]))) {
            nearBlocks = true;
        }
        return nearBlocks;
    }

    public static boolean blocksNearNotDiagonal(final Location loc) {
        boolean nearBlocks = false;
        for (Block block : UtilBlock.getSurroundingB(loc.getBlock())) {
            if (block.getType() != Material.AIR) {
                nearBlocks = true;
                break;
            }
        }
        loc.setY(loc.getY() - 0.5);
        if (loc.getBlock().getType() != Material.AIR) {
            nearBlocks = true;
        }
        List<Material> fences = Arrays.stream(Material.values()).filter(m -> m.toString().contains("FENCE") || m.toString().contains("_FENCE_GATE") || m == Material.LADDER).toList();
        if (isBlock(loc.getBlock().getRelative(BlockFace.DOWN), fences.toArray(new Material[0]))) {
            nearBlocks = true;
        }
        return nearBlocks;
    }

    public static boolean canStandOn(Block block) {
        return (!block.isLiquid()) && (block.getType() != Material.AIR);
    }

    public static boolean canStandWithin(Block block) {
        boolean isSand = block.getType() == Material.SAND;
        boolean isGravel = block.getType() == Material.GRAVEL;
        boolean solid = (block.getType().isSolid()) && (!block.getType().name().toLowerCase().contains("door")) && (!block.getType().name().toLowerCase().contains("fence")) && (!block.getType().name().toLowerCase().contains("bars")) && (!block.getType().name().toLowerCase().contains("sign"));

        return (!isSand) && (!isGravel) && (!solid);
    }

    public static boolean cantStandAt(Block block) {
        return (!canStandOn(block)) && (cantStandClose(block)) && (cantStandFar(block));
    }

    public static boolean cantStandAtBetter(Block block) {
        Block otherBlock = block.getRelative(BlockFace.DOWN);

        boolean center1 = otherBlock.getType() == Material.AIR;
        boolean north1 = otherBlock.getRelative(BlockFace.NORTH).getType() == Material.AIR;
        boolean east1 = otherBlock.getRelative(BlockFace.EAST).getType() == Material.AIR;
        boolean south1 = otherBlock.getRelative(BlockFace.SOUTH).getType() == Material.AIR;
        boolean west1 = otherBlock.getRelative(BlockFace.WEST).getType() == Material.AIR;
        boolean northeast1 = otherBlock.getRelative(BlockFace.NORTH_EAST).getType() == Material.AIR;
        boolean northwest1 = otherBlock.getRelative(BlockFace.NORTH_WEST).getType() == Material.AIR;
        boolean southeast1 = otherBlock.getRelative(BlockFace.SOUTH_EAST).getType() == Material.AIR;
        boolean southwest1 = otherBlock.getRelative(BlockFace.SOUTH_WEST).getType() == Material.AIR;
        boolean overAir1 = (otherBlock.getRelative(BlockFace.DOWN).getType() == Material.AIR) || (otherBlock.getRelative(BlockFace.DOWN).getType() == Material.WATER) || (otherBlock.getRelative(BlockFace.DOWN).getType() == Material.LAVA);

        return (center1) && (north1) && (east1) && (south1) && (west1) && (northeast1) && (southeast1) && (northwest1) && (southwest1) && (overAir1);
    }

    public static boolean cantStandAtExp(Location location) {
        return cantStandAt(new Location(location.getWorld(), fixXAxis(location.getX()), location.getY() - 0.01D, location.getBlockZ()).getBlock());
    }

    public static boolean cantStandAtSingle(Block block) {
        Block otherBlock = block.getRelative(BlockFace.DOWN);
        return otherBlock.getType() == Material.AIR;
    }

    public static boolean cantStandAtWater(Block block) {
        Block otherBlock = block.getRelative(BlockFace.DOWN);

        boolean isHover = block.getType() == Material.AIR;
        boolean n = (otherBlock.getRelative(BlockFace.NORTH).getType() == Material.WATER);
        boolean s = (otherBlock.getRelative(BlockFace.SOUTH).getType() == Material.WATER);
        boolean e = (otherBlock.getRelative(BlockFace.EAST).getType() == Material.WATER);
        boolean w = (otherBlock.getRelative(BlockFace.WEST).getType() == Material.WATER);
        boolean ne = (otherBlock.getRelative(BlockFace.NORTH_EAST).getType() == Material.WATER);
        boolean nw = (otherBlock.getRelative(BlockFace.NORTH_WEST).getType() == Material.WATER);
        boolean se = (otherBlock.getRelative(BlockFace.SOUTH_EAST).getType() == Material.WATER);
        boolean sw = (otherBlock.getRelative(BlockFace.SOUTH_WEST).getType() == Material.WATER);

        return (n) && (s) && (e) && (w) && (ne) && (nw) && (se) && (sw) && (isHover);
    }

    public static boolean cantStandClose(Block block) {
        return (!canStandOn(block.getRelative(BlockFace.NORTH))) && (!canStandOn(block.getRelative(BlockFace.EAST))) && (!canStandOn(block.getRelative(BlockFace.SOUTH))) && (!canStandOn(block.getRelative(BlockFace.WEST)));
    }

    public static boolean cantStandFar(Block block) {
        return (!canStandOn(block.getRelative(BlockFace.NORTH_WEST))) && (!canStandOn(block.getRelative(BlockFace.NORTH_EAST))) && (!canStandOn(block.getRelative(BlockFace.SOUTH_WEST))) && (!canStandOn(block.getRelative(BlockFace.SOUTH_EAST)));
    }

    public static double clamp180(double theta) {
        theta %= 360.0D;
        if (theta >= 180.0D) {
            theta -= 360.0D;
        }
        if (theta < -180.0D) {
            theta += 360.0D;
        }
        return theta;
    }

    public static double[] cursor(Player player, LivingEntity entity) {
        Location entityLoc = entity.getLocation().add(0.0D, entity.getEyeHeight(), 0.0D);
        Location playerLoc = player.getLocation().add(0.0D, player.getEyeHeight(), 0.0D);

        Vector playerRotation = new Vector(playerLoc.getYaw(), playerLoc.getPitch(), 0.0F);
        Vector expectedRotation = getRotation(playerLoc, entityLoc);

        double deltaYaw = clamp180(playerRotation.getX() - expectedRotation.getX());
        double deltaPitch = clamp180(playerRotation.getY() - expectedRotation.getY());

        double horizontalDistance = getHorizontalDistance(playerLoc, entityLoc);
        double distance = getDistance3D(playerLoc, entityLoc);

        double offsetX = deltaYaw * horizontalDistance * distance;
        double offsetY = deltaPitch * Math.abs(Math.sqrt(entityLoc.getY() - playerLoc.getY())) * distance;

        return new double[]{Math.abs(offsetX), Math.abs(offsetY)};
    }

    public static double fixXAxis(double x) {
        double touchedX = x;
        double rem = touchedX - Math.round(touchedX) + 0.01D;
        if (rem < 0.3D) {
            touchedX = NumberConversions.floor(x) - 1;
        }
        return touchedX;
    }

    public static double getAimbotoffset(Location playerLocLoc, double playerEyeHeight, LivingEntity entity) {
        Location entityLoc = entity.getLocation().add(0.0D, entity.getEyeHeight(), 0.0D);
        Location playerLoc = playerLocLoc.add(0.0D, playerEyeHeight, 0.0D);

        Vector playerRotation = new Vector(playerLoc.getYaw(), playerLoc.getPitch(), 0.0F);
        Vector expectedRotation = getRotation(playerLoc, entityLoc);

        double deltaYaw = clamp180(playerRotation.getX() - expectedRotation.getX());

        double horizontalDistance = getHorizontalDistance(playerLoc, entityLoc);
        double distance = getDistance3D(playerLoc, entityLoc);

        double offsetX = deltaYaw * horizontalDistance * distance;

        return offsetX;
    }

    public static double getAimbotoffset2(Location playerLocLoc, double playerEyeHeight, LivingEntity entity) {
        Location entityLoc = entity.getLocation().add(0.0D, entity.getEyeHeight(), 0.0D);
        Location playerLoc = playerLocLoc.add(0.0D, playerEyeHeight, 0.0D);

        Vector playerRotation = new Vector(playerLoc.getYaw(), playerLoc.getPitch(), 0.0F);
        Vector expectedRotation = getRotation(playerLoc, entityLoc);

        double deltaPitch = clamp180(playerRotation.getY() - expectedRotation.getY());

        double distance = getDistance3D(playerLoc, entityLoc);

        double offsetY = deltaPitch * Math.abs(Math.sqrt(entityLoc.getY() - playerLoc.getY())) * distance;

        return offsetY;
    }

    public static String getCardinalDirection(Player player) {
        double rotation = (player.getLocation().getYaw() - 90) % 360;
        if (rotation < 0) {
            rotation += 360.0;
        }
        if (0 <= rotation && rotation < 22.5) {
            return "N";
        } else if (22.5 <= rotation && rotation < 67.5) {
            return "NE";
        } else if (67.5 <= rotation && rotation < 112.5) {
            return "E";
        } else if (112.5 <= rotation && rotation < 157.5) {
            return "SE";
        } else if (157.5 <= rotation && rotation < 202.5) {
            return "S";
        } else if (202.5 <= rotation && rotation < 247.5) {
            return "SW";
        } else if (247.5 <= rotation && rotation < 292.5) {
            return "W";
        } else if (292.5 <= rotation && rotation < 337.5) {
            return "NW";
        } else if (337.5 <= rotation && rotation < 360.0) {
            return "N";
        } else {
            return null;
        }
    }

    public static String[] getCommands(String command) {
        return command.replaceAll("COMMAND\\[", "").replaceAll("]", "").split(";");
    }

    public static double getDistance3D(Location one, Location two) {
        double xSqr = (two.getX() - one.getX()) * (two.getX() - one.getX());
        double ySqr = (two.getY() - one.getY()) * (two.getY() - one.getY());
        double zSqr = (two.getZ() - one.getZ()) * (two.getZ() - one.getZ());
        double sqrt = Math.sqrt(xSqr + ySqr + zSqr);
        return Math.abs(sqrt);
    }

    public static double getHorizontalDistance(Location one, Location two) {
        double toReturn;
        double xSqr = (two.getX() - one.getX()) * (two.getX() - one.getX());
        double zSqr = (two.getZ() - one.getZ()) * (two.getZ() - one.getZ());
        double sqrt = Math.sqrt(xSqr + zSqr);
        toReturn = Math.abs(sqrt);
        return toReturn;
    }

    public static double getOffsetOffCursor(Player player, LivingEntity entity) {
        double offset = 0.0D;
        double[] offsets = getOffsetsOffCursor(player, entity);

        offset += offsets[0];
        offset += offsets[1];

        return offset;
    }

    public static double[] getOffsetsOffCursor(Player player, LivingEntity entity) {
        Location entityLoc = entity.getLocation().add(0.0D, entity.getEyeHeight(), 0.0D);
        Location playerLoc = player.getLocation().add(0.0D, player.getEyeHeight(), 0.0D);

        Vector playerRotation = new Vector(playerLoc.getYaw(), playerLoc.getPitch(), 0.0F);
        Vector expectedRotation = getRotation(playerLoc, entityLoc);

        double deltaYaw = clamp180(playerRotation.getX() - expectedRotation.getX());
        double deltaPitch = clamp180(playerRotation.getY() - expectedRotation.getY());

        double horizontalDistance = getHorizontalDistance(playerLoc, entityLoc);
        double distance = getDistance3D(playerLoc, entityLoc);

        double offsetX = deltaYaw * horizontalDistance * distance;
        double offsetY = deltaPitch * Math.abs(Math.sqrt(entityLoc.getY() - playerLoc.getY())) * distance;

        return new double[]{Math.abs(offsetX), Math.abs(offsetY)};
    }

    public static Vector getRotation(Location one, Location two) {
        double dx = two.getX() - one.getX();
        double dy = two.getY() - one.getY();
        double dz = two.getZ() - one.getZ();
        double distanceXZ = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.atan2(dz, dx) * 180.0D / 3.141592653589793D) - 90.0F;
        float pitch = (float) -(Math.atan2(dy, distanceXZ) * 180.0D / 3.141592653589793D);
        return new Vector(yaw, pitch, 0.0F);
    }

    public static double getVerticalDistance(Location one, Location two) {
        double toReturn;
        double ySqr = (two.getY() - one.getY()) * (two.getY() - one.getY());
        double sqrt = Math.sqrt(ySqr);
        toReturn = Math.abs(sqrt);
        return toReturn;
    }

    public static double getXDelta(Location one, Location two) {
        return Math.abs(one.getX() - two.getX());
    }

    public static double getZDelta(Location one, Location two) {
        return Math.abs(one.getZ() - two.getZ());
    }

    public static boolean hasArmorEnchantment(Player player, Enchantment e) {
        ItemStack[] arrayOfItemStack;
        int j = (arrayOfItemStack = player.getInventory().getArmorContents()).length;
        for (int i = 0; i < j; i++) {
            ItemStack is = arrayOfItemStack[i];
            if ((is != null) && (is.containsEnchantment(e))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBlock(Block block, Material[] materials) {
        Material type = block.getType();
        Material[] arrayOfMaterial;
        int j = (arrayOfMaterial = materials).length;
        for (int i = 0; i < j; i++) {
            Material m = arrayOfMaterial[i];
            if (m == type) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDoor(Block block) {
        return block.getType().toString().toLowerCase().contains("_door");
    }

    public static boolean isDouble(String string) {
        try {
            Double.parseDouble(string);
            return true;
        } catch (Exception localException) {
        }
        return false;
    }

    public static boolean isFenceGate(Block block) {
        return block.getType().toString().toLowerCase().contains("_fence_gate");
    }

    public static boolean isFood(Material m) {
        return m.isEdible();
    }

    public static boolean isFullyInWater(Location player) {
        double touchedX = fixXAxis(player.getX());

        return (new Location(player.getWorld(), touchedX, player.getY(), player.getBlockZ()).getBlock().isLiquid()) && (new Location(player.getWorld(), touchedX, Math.round(player.getY()), player.getBlockZ()).getBlock().isLiquid());
    }

    public static boolean isHoveringOverWater(Location player, int blocks) {
        for (int i = player.getBlockY(); i > player.getBlockY() - blocks; i--) {
            Block newloc = new Location(player.getWorld(), player.getBlockX(), i, player.getBlockZ()).getBlock();
            if (newloc.getType() != Material.AIR) {
                return newloc.isLiquid();
            }
        }
        return false;
    }

    public static boolean isHoveringOverWater(Location player) {
        return isHoveringOverWater(player, 25);
    }

    public static boolean isInWater(Player player) {
        return (player.getLocation().getBlock().isLiquid()) || (player.getLocation().getBlock().getRelative(BlockFace.DOWN).isLiquid()) || (player.getLocation().getBlock().getRelative(BlockFace.UP).isLiquid());
    }

    @SuppressWarnings("unlikely-arg-type")
    public static boolean isInWeb(Player player) {
        if (UtilBlock.getBlocksAroundCenter(player.getLocation(), 1).contains(Material.COBWEB)) {
            return true;
        }
        return (player.getLocation().getBlock().getType() == Material.COBWEB) || (player.getLocation().clone().add(0.0D, 1.0D, 0.0D).getBlock().getType() == Material.COBWEB) || (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.COBWEB) || (player.getLocation().getBlock().getRelative(BlockFace.UP).getType() == Material.COBWEB);
    }

    public static boolean isInstantBreak(Material m) {
        return false;
    }

    public static boolean isInt(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (Exception localException) {
        }
        return false;
    }

    public static boolean isInteractable(Material m) {
        return m.isInteractable();
    }

    public static boolean isOnFence(Location loc) {
        if (loc.clone().subtract(0, 0.51, 0).getBlock().getType().toString().endsWith("_FENCE")) {
            return true;
        }
        if (loc.clone().subtract(0, 0.51, 0).getBlock().getType().toString().endsWith("_FENCE_GATE")) {
            Gate g = (Gate) loc.clone().subtract(0, 0.51, 0).getBlock().getBlockData();
            return !g.isOpen();
        }
        return loc.clone().subtract(0, 0.51, 0).getBlock().getType().toString().endsWith("_WALL");
    }

    public static boolean isOnGround(Location loc) {
        return isOnGround(loc, 0.05);
    }

    public static boolean isOnGround(Player p) {
        return isOnGround(p.getLocation());
    }

    public static boolean isOnGround(Location locIn, double down) {

        if (UtilBlock.isClimbableBlock(locIn.getBlock())) return true;
        if (UtilBlock.isCarpet(locIn.getBlock())) return true;
        if (UtilBlock.isLiquid(locIn.getBlock())) return true;
        if (UtilCheat.isOnLilyPad(locIn)) return true;
        if (UtilCheat.isOnFence(locIn)) return true;

        List<Block> blocks = new ArrayList<>();
        Location loc = locIn.clone().subtract(0, down, 0);
        blocks.add(loc.getBlock());
        blocks.add(loc.clone().add(0.3, 0, 0).getBlock());
        blocks.add(loc.clone().add(0, 0, 0.3).getBlock());
        blocks.add(loc.clone().add(-0.3, 0, 0).getBlock());
        blocks.add(loc.clone().add(0, 0, -0.3).getBlock());
        blocks.add(loc.clone().add(0.3, 0, 0.3).getBlock());
        blocks.add(loc.clone().add(0.3, 0, -0.3).getBlock());
        blocks.add(loc.clone().add(-0.3, 0, 0.3).getBlock());
        blocks.add(loc.clone().add(-0.3, 0, -0.3).getBlock());

        for (Block b : blocks) {
            if (UtilBlock.isSolid(b)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isOnGroundOld(final Location location, final int down) {
        final double posX = location.getX();
        final double posZ = location.getZ();
        final double fracX = (UtilMath.getFraction(posX) > 0.0) ? Math.abs(UtilMath.getFraction(posX)) : (1.0 - Math.abs(UtilMath.getFraction(posX)));
        final double fracZ = (UtilMath.getFraction(posZ) > 0.0) ? Math.abs(UtilMath.getFraction(posZ)) : (1.0 - Math.abs(UtilMath.getFraction(posZ)));
        final int blockX = location.getBlockX();
        final int blockY = location.getBlockY() - down;
        final int blockZ = location.getBlockZ();
        final World world = location.getWorld();
        if (UtilBlock.isSolid(world.getBlockAt(blockX, blockY, blockZ)) || isOnLilyPad(location)) {
            return true;
        }
        if (fracX < 0.3) {
            if (UtilBlock.isSolid(world.getBlockAt(blockX - 1, blockY, blockZ))) {
                return true;
            }
            if (fracZ < 0.3) {
                if (UtilBlock.isSolid(world.getBlockAt(blockX - 1, blockY, blockZ - 1))) {
                    return true;
                }
                if (UtilBlock.isSolid(world.getBlockAt(blockX, blockY, blockZ - 1))) {
                    return true;
                }
                return UtilBlock.isSolid(world.getBlockAt(blockX + 1, blockY, blockZ - 1));
            } else if (fracZ > 0.7) {
                if (UtilBlock.isSolid(world.getBlockAt(blockX - 1, blockY, blockZ + 1))) {
                    return true;
                }
                if (UtilBlock.isSolid(world.getBlockAt(blockX, blockY, blockZ + 1))) {
                    return true;
                }
                return UtilBlock.isSolid(world.getBlockAt(blockX + 1, blockY, blockZ + 1));
            }
        } else if (fracX > 0.7) {
            if (UtilBlock.isSolid(world.getBlockAt(blockX + 1, blockY, blockZ))) {
                return true;
            }
            if (fracZ < 0.3) {
                if (UtilBlock.isSolid(world.getBlockAt(blockX - 1, blockY, blockZ - 1))) {
                    return true;
                }
                if (UtilBlock.isSolid(world.getBlockAt(blockX, blockY, blockZ - 1))) {
                    return true;
                }
                return UtilBlock.isSolid(world.getBlockAt(blockX + 1, blockY, blockZ - 1));
            } else if (fracZ > 0.7) {
                if (UtilBlock.isSolid(world.getBlockAt(blockX - 1, blockY, blockZ + 1))) {
                    return true;
                }
                if (UtilBlock.isSolid(world.getBlockAt(blockX, blockY, blockZ + 1))) {
                    return true;
                }
                return UtilBlock.isSolid(world.getBlockAt(blockX + 1, blockY, blockZ + 1));
            }
        } else if (fracZ < 0.3) {
            return UtilBlock.isSolid(world.getBlockAt(blockX, blockY, blockZ - 1));
        } else return fracZ > 0.7 && UtilBlock.isSolid(world.getBlockAt(blockX, blockY, blockZ + 1));
        return false;
    }

    public static boolean isOnLilyPad(Player player) {
        return isOnLilyPad(player.getLocation());
    }

    public static boolean isOnLilyPad(Location loc) {
        Block block = loc.getBlock();
        Material lily = Material.LILY_PAD;

        return (block.getType() == lily) || (block.getRelative(BlockFace.NORTH).getType() == lily) || (block.getRelative(BlockFace.SOUTH).getType() == lily) || (block.getRelative(BlockFace.EAST).getType() == lily) || (block.getRelative(BlockFace.WEST).getType() == lily);
    }

    public static boolean isOnVine(Player player) {
        return player.getLocation().getBlock().getType() == Material.VINE;
    }

    public static boolean isSlab(Block block) {
        if (block == null) {
            return false;
        }
        return block.getType().toString().toLowerCase().contains("_slab");
    }

    @SuppressWarnings("incomplete-switch")
    public static boolean isStair(Block block) {
        return block.getType().toString().toLowerCase().contains("_stairs");
    }

    public static boolean isSubmersed(Player player) {
        return (player.getLocation().getBlock().isLiquid()) && (player.getLocation().getBlock().getRelative(BlockFace.UP).isLiquid());
    }

    public static boolean isTrapDoor(Block block) {
        return block.getType().toString().toLowerCase().contains("_trapdoor");
    }

    public static long lifeToSeconds(String string) {
        if ((string.equals("0")) || (string.equals(""))) {
            return 0L;
        }
        String[] lifeMatch = {"d", "h", "m", "s"};
        int[] lifeInterval = {86400, 3600, 60, 1};
        long seconds = 0L;
        for (int i = 0; i < lifeMatch.length; i++) {
            Matcher matcher = Pattern.compile("([0-9]*)" + lifeMatch[i]).matcher(string);
            while (matcher.find()) {
                seconds += (long) Integer.parseInt(matcher.group(1)) * lifeInterval[i];
            }
        }
        return seconds;
    }

    public static String listToCommaString(List<String> list) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            b.append(list.get(i));
            if (i < list.size() - 1) {
                b.append(",");
            }
        }
        return b.toString();
    }

    public static String removeWhitespace(String string) {
        return string.replaceAll(" ", "");
    }

    public static boolean slabsNear(Location loc) {
        boolean nearBlocks = false;
        for (Block bl : UtilBlock.getSurrounding(loc.getBlock(), true)) {
            if (isSlab(bl)) {
                nearBlocks = true;
                break;
            }
        }
        for (Block bl : UtilBlock.getSurrounding(loc.getBlock(), false)) {
            if (isSlab(bl)) {
                nearBlocks = true;
                break;
            }
        }
        if (isSlab(loc.getBlock().getRelative(BlockFace.DOWN))) {
            nearBlocks = true;
        }
        return nearBlocks;
    }

    public static boolean sprintFly(Player player) {
        return (player.isSprinting()) || (player.isFlying());
    }

}
