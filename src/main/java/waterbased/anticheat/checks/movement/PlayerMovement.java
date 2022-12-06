package waterbased.anticheat.checks.movement;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import waterbased.anticheat.AntiCheat;
import waterbased.anticheat.events.PlayerOnGroundChangeEvent;
import waterbased.anticheat.events.PlayerPreciseMoveEvent;
import waterbased.anticheat.utils.UtilBlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PlayerMovement implements Listener {

    private static final HashMap<Player, Location> lastOnGround = new HashMap<>();
    private static final HashMap<Player, Location> highestSinceGround = new HashMap<>();
    private static final HashMap<Player, Location> lastInLiquid = new HashMap<>();
    private static final HashMap<Player, Location> lastOnClimbable = new HashMap<>();
    private static final HashMap<Player, Location> lastInWebs = new HashMap<>();
    private static final HashMap<Player, Boolean> isOnGround = new HashMap<>();
    private static final HashMap<Player, Boolean> isOnClimbable = new HashMap<>();
    private static final HashMap<Player, Boolean> isInLiquid = new HashMap<>();
    private static final HashMap<Player, Boolean> isInWebs = new HashMap<>();
    private static final HashMap<Player, Location> lastLocation = new HashMap<>();
    private static final HashMap<Player, Location> lastSafeLocation = new HashMap<>();

    private static boolean callEvent(Player player, Location from, Location to, boolean onGround) {
        PlayerPreciseMoveEvent event = new PlayerPreciseMoveEvent(player, from, to, onGround);
        Bukkit.getScheduler().runTask(AntiCheat.instance, () -> {
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                player.teleport(from);
            } else {
                lastLocation.put(player, event.getTo());
            }
        });
        return true;
    }

    private static List<Block> getBlocksOfBoundingBox(BoundingBox box, World world) {
        Block b1 = box.getMax().toLocation(world).getBlock();
        Block b2 = box.getMax().add(new Vector(-box.getWidthX(), 0, 0)).toLocation(world).getBlock();
        Block b3 = box.getMax().add(new Vector(0, 0, -box.getWidthZ())).toLocation(world).getBlock();
        Block b4 = box.getMax().add(new Vector(-box.getWidthX(), 0, -box.getWidthZ())).toLocation(world).getBlock();
        Block b5 = box.getMin().toLocation(world).getBlock();
        Block b6 = box.getMin().add(new Vector(box.getWidthX(), 0, 0)).toLocation(world).getBlock();
        Block b7 = box.getMin().add(new Vector(0, 0, box.getWidthZ())).toLocation(world).getBlock();
        Block b8 = box.getMin().add(new Vector(box.getWidthX(), 0, box.getWidthZ())).toLocation(world).getBlock();
        return Arrays.asList(b1, b2, b3, b4, b5, b6, b7, b8);
    }

    public static Location getHighestSinceGround(Player player) {
        return highestSinceGround.getOrDefault(player, player.getLocation());
    }

    public static Location getLastInLiquid(Player player) {
        return lastInLiquid.getOrDefault(player, player.getLocation());
    }

    public static Location getLastInWebs(Player player) {
        return lastInWebs.getOrDefault(player, player.getLocation());
    }

    public static Location getLastOnClimbable(Player player) {
        return lastOnClimbable.getOrDefault(player, player.getLocation());
    }

    public static Location getLastOnGround(Player player) {
        return lastOnGround.getOrDefault(player, player.getLocation());
    }

    public static Location getLastSafeLocation(Player player) {
        return lastSafeLocation.getOrDefault(player, player.getLocation());
    }

    public static boolean groundPacket(Player player, boolean onGround) {
        Location from = lastLocation.getOrDefault(player, player.getLocation());
        return movePacket(player, from.getX(), from.getY(), from.getZ(), from.getYaw(), from.getPitch(), onGround);
    }

    public static boolean inLava(Player player) {
        return inLava(player.getBoundingBox(), player.getWorld());
    }

    private static boolean inLava(BoundingBox box, World world) {
        for (Block block : getBlocksOfBoundingBox(box, world)) {
            if (block.getType() == Material.LAVA) {
                return true;
            }
        }
        return false;
    }

    public static boolean inLiquid(Player player) {
        return isInLiquid.getOrDefault(player, inLiquid(player.getBoundingBox(), player.getWorld()));
    }

    private static boolean inLiquid(BoundingBox box, World world) {
        for (Block block : getBlocksOfBoundingBox(box, world)) {
            if (block.isLiquid()) {
                return true;
            }
        }
        return false;
    }

    public static boolean inWater(Player player) {
        return inWater(player.getBoundingBox(), player.getWorld());
    }

    private static boolean inWater(BoundingBox box, World world) {
        for (Block block : getBlocksOfBoundingBox(box, world)) {
            if (block.getType() == Material.WATER) {
                return true;
            }
        }
        return false;
    }

    public static boolean inWebs(Player player) {
        return isInWebs.getOrDefault(player, inWebs(player.getBoundingBox(), player.getWorld()));
    }

    private static boolean inWebs(BoundingBox box, World world) {
        for (Block block : getBlocksOfBoundingBox(box, world)) {
            if (block.getType() == Material.COBWEB) {
                return true;
            }
        }
        return false;
    }

    public static boolean isClimbing(Player player) {
        return isOnClimbable.getOrDefault(player, isClimbing(player.getBoundingBox(), player.getWorld()));
    }

    private static boolean isClimbing(BoundingBox box, World world) {
        for (Block block : getBlocksOfBoundingBox(box, world)) {
            if (UtilBlock.isClimbableBlock(block)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOnGround(Player player) {
        return isOnGround.getOrDefault(player, isOnGround(player.getLocation(), player.getBoundingBox(), 0.05));
    }

    public static boolean isOnGround(Location inLoc, BoundingBox boundingBox, double down) {

        List<Block> blocks = new ArrayList<>();
        Location loc = inLoc.clone().subtract(0, down, 0);
        blocks.add(loc.clone().getBlock());
        blocks.add(loc.clone().add(0.3, 0, 0).getBlock());
        blocks.add(loc.clone().add(0, 0, 0.3).getBlock());
        blocks.add(loc.clone().add(-0.3, 0, 0).getBlock());
        blocks.add(loc.clone().add(0, 0, -0.3).getBlock());
        blocks.add(loc.clone().add(0.3, 0, 0.3).getBlock());
        blocks.add(loc.clone().add(0.3, 0, -0.3).getBlock());
        blocks.add(loc.clone().add(-0.3, 0, 0.3).getBlock());
        blocks.add(loc.clone().add(-0.3, 0, -0.3).getBlock());

        BoundingBox bb = boundingBox.clone().shift(-inLoc.getBlockX(), -inLoc.getBlockY() - down, -inLoc.getBlockZ());

        for (Block b : blocks) {
            if (b.isCollidable()) {
                if (b.getCollisionShape().overlaps(bb)) {
                    return true;
                }
            }
        }
        for (Block bi : blocks) {
            Block b = bi.getRelative(BlockFace.DOWN);
            if (b.getType().toString().endsWith("_FENCE") || b.getType().toString().endsWith("_FENCE_GATE") || b.getType().toString().endsWith("_WALL")) {
                if (b.getCollisionShape().overlaps(bb)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean lookPacket(Player player, float yaw, float pitch, boolean onGround) {
        Location from = lastLocation.getOrDefault(player, player.getLocation());
        return movePacket(player, from.getX(), from.getY(), from.getZ(), yaw, pitch, onGround);
    }

    public static boolean movePacket(Player player, double x, double y, double z, boolean onGround) {
        Location from = lastLocation.get(player);
        if (from == null) {
            return movePacket(player, x, y, z, 0, 0, onGround);
        } else {
            return movePacket(player, x, y, z, from.getYaw(), from.getPitch(), onGround);
        }
    }

    public static boolean movePacket(Player player, double x, double y, double z, float yaw, float pitch, boolean onGround) {
        Location from = lastLocation.getOrDefault(player, player.getLocation());
        Location to = new Location(from.getWorld(), x, y, z, yaw, pitch);
        onMove(player, from, to, onGround);
        return callEvent(player, from, to, onGround);
    }

    private static void onMove(Player player, Location from, Location to, boolean clientOnGround) {

        BoundingBox box = player.getBoundingBox().clone();

        if (inLiquid(box, to.getWorld())) {
            lastInLiquid.put(player, to);
            isInLiquid.put(player, true);
            lastSafeLocation.put(player, to);
        } else {
            isInLiquid.put(player, false);
        }

        if (isClimbing(box, to.getWorld())) {
            lastOnClimbable.put(player, to);
            isOnClimbable.put(player, true);
            lastSafeLocation.put(player, to);
        } else {
            isOnClimbable.put(player, false);
        }

        if (inWebs(box, to.getWorld())) {
            lastInWebs.put(player, to);
            isInWebs.put(player, true);
            lastSafeLocation.put(player, to);
        } else {
            isInWebs.put(player, false);
        }

        if (isOnGround(to, box, 0.05)) {
            if (!isOnGround.getOrDefault(player, false)) {
                double fallDistance = highestSinceGround.getOrDefault(player, from).getY() - to.getY();
                PlayerOnGroundChangeEvent event = new PlayerOnGroundChangeEvent(player, true, fallDistance);
                Bukkit.getScheduler().runTask(AntiCheat.instance, () -> Bukkit.getPluginManager().callEvent(event));
            }
            lastOnGround.put(player, to);
            highestSinceGround.put(player, to);
            isOnGround.put(player, true);
            lastSafeLocation.put(player, to);
        } else {
            isOnGround.put(player, false);
            if (highestSinceGround.getOrDefault(player, to).getY() < to.getY()) {
                highestSinceGround.put(player, to);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTeleport(PlayerTeleportEvent e) {
        lastOnGround.put(e.getPlayer(), e.getTo());
        highestSinceGround.put(e.getPlayer(), e.getTo());
        lastOnClimbable.put(e.getPlayer(), e.getTo());
    }

}
