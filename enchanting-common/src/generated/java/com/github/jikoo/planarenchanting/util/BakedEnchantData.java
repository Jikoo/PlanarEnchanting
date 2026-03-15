package com.github.jikoo.planarenchanting.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import javax.annotation.processing.Generated;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Pre-baked data used for {@link MetaEnchantProvider}.
 *
 * <p>This file was generated from Minecraft 1.21.11. Regenerate it rather than modify.</p>
 */
@Generated("com.github.jikoo.planarenchanting.generator.impl.EnchantDataGenerator")
@NullMarked
final class BakedEnchantData {

  static Map<@Nullable NamespacedKey, Function<Enchantment, EnchantData>> get() {
    Map<@Nullable NamespacedKey, Function<Enchantment, EnchantData>> map = new HashMap<>();
    // <editor-fold defaultstate="collapsed" desc="Generated from net.minecraft.world.item.enchantment.Enchantments">
    map.put(NamespacedKey.fromString("minecraft:loyalty"), create(5, 2, lvl -> 12 + 7 * (lvl - 1), lvl -> 50));
    map.put(NamespacedKey.fromString("minecraft:respiration"), create(2, 4, lvl -> 10 + 10 * (lvl - 1), lvl -> 40 + 10 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:wind_burst"), create(2, 4, lvl -> 15 + 9 * (lvl - 1), lvl -> 65 + 9 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:fire_protection"), create(5, 2, lvl -> 10 + 8 * (lvl - 1), lvl -> 18 + 8 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:depth_strider"), create(2, 4, lvl -> 10 + 10 * (lvl - 1), lvl -> 25 + 10 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:channeling"), create(1, 8, lvl -> 25, lvl -> 50));
    map.put(NamespacedKey.fromString("minecraft:quick_charge"), create(5, 2, lvl -> 12 + 20 * (lvl - 1), lvl -> 50));
    map.put(NamespacedKey.fromString("minecraft:multishot"), create(2, 4, lvl -> 20, lvl -> 50));
    map.put(NamespacedKey.fromString("minecraft:vanishing_curse"), create(1, 8, lvl -> 25, lvl -> 50));
    map.put(NamespacedKey.fromString("minecraft:fire_aspect"), create(2, 4, lvl -> 10 + 20 * (lvl - 1), lvl -> 60 + 20 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:looting"), create(2, 4, lvl -> 15 + 9 * (lvl - 1), lvl -> 65 + 9 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:bane_of_arthropods"), create(5, 2, lvl -> 5 + 8 * (lvl - 1), lvl -> 25 + 8 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:blast_protection"), create(2, 4, lvl -> 5 + 8 * (lvl - 1), lvl -> 13 + 8 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:frost_walker"), create(2, 4, lvl -> 10 + 10 * (lvl - 1), lvl -> 25 + 10 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:smite"), create(5, 2, lvl -> 5 + 8 * (lvl - 1), lvl -> 25 + 8 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:impaling"), create(2, 4, lvl -> 1 + 8 * (lvl - 1), lvl -> 21 + 8 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:mending"), create(2, 4, lvl -> 25 + 25 * (lvl - 1), lvl -> 75 + 25 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:sharpness"), create(10, 1, lvl -> 1 + 11 * (lvl - 1), lvl -> 21 + 11 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:unbreaking"), create(5, 2, lvl -> 5 + 8 * (lvl - 1), lvl -> 55 + 8 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:efficiency"), create(10, 1, lvl -> 1 + 10 * (lvl - 1), lvl -> 51 + 10 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:lure"), create(2, 4, lvl -> 15 + 9 * (lvl - 1), lvl -> 65 + 9 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:protection"), create(10, 1, lvl -> 1 + 11 * (lvl - 1), lvl -> 12 + 11 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:binding_curse"), create(1, 8, lvl -> 25, lvl -> 50));
    map.put(NamespacedKey.fromString("minecraft:thorns"), create(1, 8, lvl -> 10 + 20 * (lvl - 1), lvl -> 60 + 20 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:silk_touch"), create(1, 8, lvl -> 15, lvl -> 65));
    map.put(NamespacedKey.fromString("minecraft:breach"), create(2, 4, lvl -> 15 + 9 * (lvl - 1), lvl -> 65 + 9 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:riptide"), create(2, 4, lvl -> 17 + 7 * (lvl - 1), lvl -> 50));
    map.put(NamespacedKey.fromString("minecraft:luck_of_the_sea"), create(2, 4, lvl -> 15 + 9 * (lvl - 1), lvl -> 65 + 9 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:flame"), create(2, 4, lvl -> 20, lvl -> 50));
    map.put(NamespacedKey.fromString("minecraft:density"), create(5, 2, lvl -> 5 + 8 * (lvl - 1), lvl -> 25 + 8 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:projectile_protection"), create(5, 2, lvl -> 3 + 6 * (lvl - 1), lvl -> 9 + 6 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:feather_falling"), create(5, 2, lvl -> 5 + 6 * (lvl - 1), lvl -> 11 + 6 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:sweeping_edge"), create(2, 4, lvl -> 5 + 9 * (lvl - 1), lvl -> 20 + 9 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:swift_sneak"), create(1, 8, lvl -> 25 + 25 * (lvl - 1), lvl -> 75 + 25 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:infinity"), create(1, 8, lvl -> 20, lvl -> 50));
    map.put(NamespacedKey.fromString("minecraft:piercing"), create(10, 1, lvl -> 1 + 10 * (lvl - 1), lvl -> 50));
    map.put(NamespacedKey.fromString("minecraft:fortune"), create(2, 4, lvl -> 15 + 9 * (lvl - 1), lvl -> 65 + 9 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:lunge"), create(5, 2, lvl -> 5 + 8 * (lvl - 1), lvl -> 25 + 8 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:punch"), create(2, 4, lvl -> 12 + 20 * (lvl - 1), lvl -> 37 + 20 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:knockback"), create(5, 2, lvl -> 5 + 20 * (lvl - 1), lvl -> 55 + 20 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:soul_speed"), create(1, 8, lvl -> 10 + 10 * (lvl - 1), lvl -> 25 + 10 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:power"), create(10, 1, lvl -> 1 + 10 * (lvl - 1), lvl -> 16 + 10 * (lvl - 1)));
    map.put(NamespacedKey.fromString("minecraft:aqua_affinity"), create(2, 4, lvl -> 1, lvl -> 41));
    // </editor-fold>
    return map;
  }

  static Function<Enchantment, EnchantData> create(int weight, int anvilCost,
      IntUnaryOperator minModCost, IntUnaryOperator maxModCost) {
    return enchant -> new EnchantData() {
      @Override
      public int getWeight() {
        return weight;
      }

      @Override
      public int getAnvilCost() {
        return anvilCost;
      }

      @Override
      public int getMinModifiedCost(int level) {
        return minModCost.applyAsInt(level);
      }

      @Override
      public int getMaxModifiedCost(int level) {
        return maxModCost.applyAsInt(level);
      }

      @Override
      public boolean isTridentEnchant() {
        return enchant.canEnchantItem(new ItemStack(Material.TRIDENT)) && !enchant.canEnchantItem(new ItemStack(Material.DIAMOND_SWORD));
      }
    };
  }

}
