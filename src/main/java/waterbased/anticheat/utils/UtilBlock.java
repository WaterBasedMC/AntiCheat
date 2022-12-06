package waterbased.anticheat.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class UtilBlock {

    private static final HashSet<Material> matClimbable = new HashSet<>();

    static {
        matClimbable.add(Material.VINE);
        matClimbable.add(Material.LADDER);
        matClimbable.add(Material.SCAFFOLDING);
        matClimbable.add(Material.WEEPING_VINES);
        matClimbable.add(Material.WEEPING_VINES_PLANT);
        matClimbable.add(Material.TWISTING_VINES);
        matClimbable.add(Material.TWISTING_VINES_PLANT);
        matClimbable.add(Material.GLOW_BERRIES);
        // & All trapdoors (see isClimbableBlock)
    }

    public static String LocationToString(Location Location) {
        return Location.getWorld().getName() + "," + Location.getX() + "," + Location.getY() + "," + Location.getZ()
                + "," + Location.getPitch() + "," + Location.getYaw();
    }

    public static Location StringToLocation(String Key) {
        String[] Args = Key.split(",");
        World World = Bukkit.getWorld(Args[0]);
        double X = Double.parseDouble(Args[1]);
        double Y = Double.parseDouble(Args[2]);
        double Z = Double.parseDouble(Args[3]);
        float Pitch = Float.parseFloat(Args[4]);
        float Yaw = Float.parseFloat(Args[5]);
        return new Location(World, X, Y, Z, Pitch, Yaw);
    }

    public static boolean containsBlock(Location location, Material material) {
        for (int y = 0; y < location.getWorld().getMaxHeight(); y++) {
            Block Current = location.getWorld().getBlockAt((int) location.getX(), y, (int) location.getZ());
            if ((Current != null) && (Current.getType().equals(material))) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsBlock(Location location) {
        for (int y = 0; y < location.getWorld().getMaxHeight(); y++) {
            Block Current = location.getWorld().getBlockAt((int) location.getX(), y, (int) location.getZ());
            if ((Current != null) && (!Current.getType().equals(Material.AIR))) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsBlockBelow(Location location) {
        for (int y = 0; y < (int) location.getY(); y++) {
            Block Current = location.getWorld().getBlockAt((int) location.getX(), y, (int) location.getZ());
            if ((Current != null) && (!Current.getType().equals(Material.AIR))) {
                return true;
            }
        }
        return false;
    }

    public static Location deserializeLocation(String string) {
        if (string == null) {
            return null;
        }
        String[] parts = string.split(",");
        World world = Bukkit.getServer().getWorld(parts[0]);
        Double LX = Double.valueOf(Double.parseDouble(parts[1]));
        Double LY = Double.valueOf(Double.parseDouble(parts[2]));
        Double LZ = Double.valueOf(Double.parseDouble(parts[3]));
        Float P = Float.valueOf(Float.parseFloat(parts[4]));
        Float Y = Float.valueOf(Float.parseFloat(parts[5]));
        Location result = new Location(world, LX.doubleValue(), LY.doubleValue(), LZ.doubleValue());
        result.setPitch(P.floatValue());
        result.setYaw(Y.floatValue());
        return result;
    }

    public static boolean fullSolid(Block block) {
        if (block == null) {
            return false;
        }
        return isSolid(block) && block.getBoundingBox().getWidthX() == 1.0D
                && block.getBoundingBox().getWidthZ() == 1.0D && block.getBoundingBox().getHeight() == 1.0D;
    }

    public static ArrayList<Block> getBlocksAroundCenter(Location loc, int radius) {
        ArrayList<Block> blocks = new ArrayList<Block>();
        for (int x = loc.getBlockX() - radius; x <= loc.getBlockX() + radius; x++) {
            for (int y = loc.getBlockY() - radius; y <= loc.getBlockY() + radius; y++) {
                for (int z = loc.getBlockZ() - radius; z <= loc.getBlockZ() + radius; z++) {
                    Location l = new Location(loc.getWorld(), x, y, z);
                    if (l.distance(loc) <= radius) {
                        blocks.add(l.getBlock());
                    }
                }
            }
        }
        return blocks;
    }

    public static Block getHighest(Location locaton) {
        return getHighest(locaton, null);
    }

    public static Block getHighest(Location location, HashSet<Material> ignore) {
        Location loc = location;
        loc.setY(0);
        for (int i = 0; i < loc.getWorld().getMaxHeight(); i++) {
            loc.setY(loc.getWorld().getMaxHeight() - i);
            if (isSolid(loc.getBlock())) {
                break;
            }
        }
        return loc.getBlock().getRelative(BlockFace.UP);
    }

    public static HashMap<Block, Double> getInRadius(Location loc, double dR) {
        return getInRadius(loc, dR, 999.0D);
    }

    public static HashMap<Block, Double> getInRadius(Location loc, double dR, double heightLimit) {
        HashMap<Block, Double> blockList = new HashMap<Block, Double>();
        int iR = (int) dR + 1;
        for (int x = -iR; x <= iR; x++) {
            for (int z = -iR; z <= iR; z++) {
                for (int y = -iR; y <= iR; y++) {
                    if (Math.abs(y) <= heightLimit) {
                        Block curBlock = loc.getWorld().getBlockAt((int) (loc.getX() + x), (int) (loc.getY() + y),
                                (int) (loc.getZ() + z));

                        double offset = UtilMath.offset(loc, curBlock.getLocation().add(0.5D, 0.5D, 0.5D));
                        if (offset <= dR) {
                            blockList.put(curBlock, Double.valueOf(1.0D - offset / dR));
                        }
                    }
                }
            }
        }
        return blockList;
    }

    public static HashMap<Block, Double> getInRadius(Block block, double dR) {
        HashMap<Block, Double> blockList = new HashMap<Block, Double>();
        int iR = (int) dR + 1;
        for (int x = -iR; x <= iR; x++) {
            for (int z = -iR; z <= iR; z++) {
                for (int y = -iR; y <= iR; y++) {
                    Block curBlock = block.getRelative(x, y, z);

                    double offset = UtilMath.offset(block.getLocation(), curBlock.getLocation());
                    if (offset <= dR) {
                        blockList.put(curBlock, Double.valueOf(1.0D - offset / dR));
                    }
                }
            }
        }
        return blockList;
    }

    public static Block getLowestBlockAt(Location Location) {
        Block Block = Location.getWorld().getBlockAt((int) Location.getX(), 0, (int) Location.getZ());
        if ((Block == null) || (Block.getType().equals(Material.AIR))) {
            Block = Location.getBlock();
            for (int y = (int) Location.getY(); y > 0; y--) {
                Block Current = Location.getWorld().getBlockAt((int) Location.getX(), y, (int) Location.getZ());
                Block Below = Current.getLocation().clone().subtract(0.0D, 1.0D, 0.0D).getBlock();
                if ((Below == null) || (Below.getType().equals(Material.AIR))) {
                    Block = Current;
                }
            }
        }
        return Block;
    }

    public static ArrayList<Block> getSurrounding(Block block, boolean diagonals) {
        ArrayList<Block> blocks = new ArrayList<Block>();
        if (diagonals) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if ((x != 0) || (y != 0) || (z != 0)) {
                            blocks.add(block.getRelative(x, y, z));
                        }
                    }
                }
            }
        } else {
            blocks.add(block.getRelative(BlockFace.UP));
            blocks.add(block.getRelative(BlockFace.DOWN));
            blocks.add(block.getRelative(BlockFace.NORTH));
            blocks.add(block.getRelative(BlockFace.SOUTH));
            blocks.add(block.getRelative(BlockFace.EAST));
            blocks.add(block.getRelative(BlockFace.WEST));
        }
        return blocks;
    }

    public static ArrayList<Block> getSurroundingB(Block block) {
        ArrayList<Block> blocks = new ArrayList<Block>();
        for (double x = -0.5; x <= 0.5; x += 0.5) {
            for (double y = -0.5; y <= 0.5; y += 0.5) {
                for (double z = -0.5; z <= 0.5; z += 0.5) {
                    if ((x != 0) || (y != 0) || (z != 0)) {
                        blocks.add(block.getLocation().add(x, y, z).getBlock());
                    }
                }
            }
        }
        return blocks;
    }

    public static ArrayList<Block> getSurroundingXZ(Block block) {
        ArrayList<Block> blocks = new ArrayList<Block>();
        blocks.add(block.getRelative(BlockFace.NORTH));
        blocks.add(block.getRelative(BlockFace.NORTH_EAST));
        blocks.add(block.getRelative(BlockFace.NORTH_WEST));
        blocks.add(block.getRelative(BlockFace.SOUTH));
        blocks.add(block.getRelative(BlockFace.SOUTH_EAST));
        blocks.add(block.getRelative(BlockFace.SOUTH_WEST));
        blocks.add(block.getRelative(BlockFace.EAST));
        blocks.add(block.getRelative(BlockFace.WEST));

        return blocks;
    }

    public static ArrayList<Block> getSurroundingXZ(Block block, boolean diagonals) {
        ArrayList<Block> blocks = new ArrayList<Block>();
        if (diagonals) {
            blocks.add(block.getRelative(BlockFace.NORTH));
            blocks.add(block.getRelative(BlockFace.NORTH_EAST));
            blocks.add(block.getRelative(BlockFace.NORTH_WEST));
            blocks.add(block.getRelative(BlockFace.SOUTH));
            blocks.add(block.getRelative(BlockFace.SOUTH_EAST));
            blocks.add(block.getRelative(BlockFace.SOUTH_WEST));
            blocks.add(block.getRelative(BlockFace.EAST));
            blocks.add(block.getRelative(BlockFace.WEST));
        } else {
            blocks.add(block.getRelative(BlockFace.NORTH));
            blocks.add(block.getRelative(BlockFace.SOUTH));
            blocks.add(block.getRelative(BlockFace.EAST));
            blocks.add(block.getRelative(BlockFace.WEST));
        }

        return blocks;
    }

    public static boolean isBlock(ItemStack item) {
        return item.getType().isBlock();
    }

    public static boolean isCarpet(Block material) {
        return material.getType().toString().contains("_CARPET");
    }

    public static boolean isClimbableBlock(Block block) {
        return matClimbable.contains(block.getType())
                || (block.getType().toString().endsWith("_TRAPDOOR")
                    && matClimbable.contains(block.getRelative(BlockFace.DOWN).getType()));
    }

    public static boolean isInAir(Player player) {
        boolean nearBlocks = false;
        for (Block block : getSurrounding(player.getLocation().getBlock(), true)) {
            if (block.getType() != Material.AIR) {
                nearBlocks = true;
                break;
            }
        }
        return nearBlocks;
    }

    public static boolean isInSolidBlock(Location loc) {
        return isSolid(loc.getBlock());
    }

    public static boolean isInteractable(Block block) {
        return block.getType().isInteractable();
    }

    public static boolean isLiquid(Block block) {
        return block != null && (block.getType() == Material.WATER || block.getType() == Material.LAVA);
    }

    public static boolean isPowderSnow(Block block) {
        return block.getType() == Material.POWDER_SNOW;
    }

    public static boolean isSolid(Block b) {
        return b.getType().isSolid() && b.getType().isBlock() && b.getType().isCollidable();
    }

    public static boolean isVisible(Block block) {
        for (Block other : getSurrounding(block, false)) {
            if (!other.getType().isOccluding()) {
                return true;
            }
        }
        return false;
    }

    public static String serializeLocation(Location location) {
        int X = (int) location.getX();
        int Y = (int) location.getY();
        int Z = (int) location.getZ();
        int P = (int) location.getPitch();
        int Yaw = (int) location.getYaw();
        return location.getWorld().getName() + "," + X + "," + Y + "," + Z + "," + P + "," + Yaw;
    }

}