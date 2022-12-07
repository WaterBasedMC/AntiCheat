package waterbased.anticheat.utils;

import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import waterbased.anticheat.checks.movement.PlayerMovement;

public class UtilDamage {

    public static void dealFallDamage(Player player, double fallDistance) {
        dealFallDamage(player, fallDistance, true);
    }

    /**
     * Deals fall damage to a player
     *
     * @param player         The player to deal fall damage to
     * @param inFallDistance The fall distance to deal
     * @param environment    Whether the environment should be taken into account (Landing block, potion effects, etc.)
     */
    public static void dealFallDamage(Player player, double inFallDistance, boolean environment) {
        if (!environment) {
            double damage = inFallDistance - 3.0;
            if (damage < 1) return;
            player.damage(damage);
        }
        double damage = getFallDamage(player, inFallDistance);
        player.playSound(player.getLocation(), player.getFallDamageSound((int) Math.floor(inFallDistance)), SoundCategory.MASTER, 1.0f, 1.0f);
        player.damage(damage);
    }

    public static double getFallDamage(Player player, double inFallDistance) {

        double fallDistance = inFallDistance;

        if (PlayerMovement.inWater(player) || PlayerMovement.inWebs(player) || PlayerMovement.isClimbing(player)) {
            return 0.0D;
        }

        if (PlayerMovement.inLava(player)) {
            fallDistance /= 2.0d;
        }

        if (player.hasPotionEffect(PotionEffectType.JUMP)) {
            fallDistance -= (player.getPotionEffect(PotionEffectType.JUMP).getAmplifier() + 1);
        }

        double damage = fallDistance - 3.0;
        if (damage < 1) return damage;

        if (player.getInventory().getBoots() != null && player.getInventory().getBoots().getEnchantments().containsKey(Enchantment.PROTECTION_FALL)) {
            damage *= 1.0d - (player.getInventory().getBoots().getEnchantmentLevel(Enchantment.PROTECTION_FALL) * 0.12);
        }

        int armorPoints = 0;
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item == null) continue;
            armorPoints += item.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
        }
        damage *= (1.0d - (armorPoints * 0.04d));

        Block playerBlock = player.getLocation().getBlock();
        Block landingBlock = player.getLocation().clone().subtract(0, 0.4, 0).getBlock();

        if (playerBlock.getType().toString().endsWith("_BED") || landingBlock.getType().toString().endsWith("_BED")) {
            damage /= 2.0d;
        } else if (playerBlock.getType() == Material.SWEET_BERRY_BUSH) {
            damage = 0;
            return 0.0;
        } else if (landingBlock.getType() == Material.HAY_BLOCK) {
            damage *= 0.2d;
        }

        return damage + 1;
    }

}
