package com.github.jikoo.planarenchanting.table;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.processing.Generated;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Pre-baked data used as a fallthrough for {@link Enchantable}.
 *
 * <p>This file was generated from Minecraft 1.21.11. Regenerate it rather than modify.</p>
 */
@Generated("com.github.jikoo.planarenchanting.generator.impl.EnchantableGenerator")
@NullMarked
final class BakedEnchantableData {

  private BakedEnchantableData() {}

  static Map<Integer, Set<@Nullable NamespacedKey>> get() {
    Map<Integer, Set<@Nullable NamespacedKey>> map = new HashMap<>();
    Function<Integer, Set<@Nullable NamespacedKey>> create = ignored -> new HashSet<>();
    // <editor-fold defaultstate="collapsed" desc="Generated from net.minecraft.world.item.Items">
    map.computeIfAbsent(1, create).add(NamespacedKey.fromString("minecraft:book"));
    map.computeIfAbsent(1, create).add(NamespacedKey.fromString("minecraft:bow"));
    map.computeIfAbsent(12, create).add(NamespacedKey.fromString("minecraft:chainmail_boots"));
    map.computeIfAbsent(12, create).add(NamespacedKey.fromString("minecraft:chainmail_chestplate"));
    map.computeIfAbsent(12, create).add(NamespacedKey.fromString("minecraft:chainmail_helmet"));
    map.computeIfAbsent(12, create).add(NamespacedKey.fromString("minecraft:chainmail_leggings"));
    map.computeIfAbsent(13, create).add(NamespacedKey.fromString("minecraft:copper_axe"));
    map.computeIfAbsent(8, create).add(NamespacedKey.fromString("minecraft:copper_boots"));
    map.computeIfAbsent(8, create).add(NamespacedKey.fromString("minecraft:copper_chestplate"));
    map.computeIfAbsent(8, create).add(NamespacedKey.fromString("minecraft:copper_helmet"));
    map.computeIfAbsent(13, create).add(NamespacedKey.fromString("minecraft:copper_hoe"));
    map.computeIfAbsent(8, create).add(NamespacedKey.fromString("minecraft:copper_leggings"));
    map.computeIfAbsent(13, create).add(NamespacedKey.fromString("minecraft:copper_pickaxe"));
    map.computeIfAbsent(13, create).add(NamespacedKey.fromString("minecraft:copper_shovel"));
    map.computeIfAbsent(13, create).add(NamespacedKey.fromString("minecraft:copper_spear"));
    map.computeIfAbsent(13, create).add(NamespacedKey.fromString("minecraft:copper_sword"));
    map.computeIfAbsent(1, create).add(NamespacedKey.fromString("minecraft:crossbow"));
    map.computeIfAbsent(10, create).add(NamespacedKey.fromString("minecraft:diamond_axe"));
    map.computeIfAbsent(10, create).add(NamespacedKey.fromString("minecraft:diamond_boots"));
    map.computeIfAbsent(10, create).add(NamespacedKey.fromString("minecraft:diamond_chestplate"));
    map.computeIfAbsent(10, create).add(NamespacedKey.fromString("minecraft:diamond_helmet"));
    map.computeIfAbsent(10, create).add(NamespacedKey.fromString("minecraft:diamond_hoe"));
    map.computeIfAbsent(10, create).add(NamespacedKey.fromString("minecraft:diamond_leggings"));
    map.computeIfAbsent(10, create).add(NamespacedKey.fromString("minecraft:diamond_pickaxe"));
    map.computeIfAbsent(10, create).add(NamespacedKey.fromString("minecraft:diamond_shovel"));
    map.computeIfAbsent(10, create).add(NamespacedKey.fromString("minecraft:diamond_spear"));
    map.computeIfAbsent(10, create).add(NamespacedKey.fromString("minecraft:diamond_sword"));
    map.computeIfAbsent(1, create).add(NamespacedKey.fromString("minecraft:fishing_rod"));
    map.computeIfAbsent(22, create).add(NamespacedKey.fromString("minecraft:golden_axe"));
    map.computeIfAbsent(25, create).add(NamespacedKey.fromString("minecraft:golden_boots"));
    map.computeIfAbsent(25, create).add(NamespacedKey.fromString("minecraft:golden_chestplate"));
    map.computeIfAbsent(25, create).add(NamespacedKey.fromString("minecraft:golden_helmet"));
    map.computeIfAbsent(22, create).add(NamespacedKey.fromString("minecraft:golden_hoe"));
    map.computeIfAbsent(25, create).add(NamespacedKey.fromString("minecraft:golden_leggings"));
    map.computeIfAbsent(22, create).add(NamespacedKey.fromString("minecraft:golden_pickaxe"));
    map.computeIfAbsent(22, create).add(NamespacedKey.fromString("minecraft:golden_shovel"));
    map.computeIfAbsent(22, create).add(NamespacedKey.fromString("minecraft:golden_spear"));
    map.computeIfAbsent(22, create).add(NamespacedKey.fromString("minecraft:golden_sword"));
    map.computeIfAbsent(14, create).add(NamespacedKey.fromString("minecraft:iron_axe"));
    map.computeIfAbsent(9, create).add(NamespacedKey.fromString("minecraft:iron_boots"));
    map.computeIfAbsent(9, create).add(NamespacedKey.fromString("minecraft:iron_chestplate"));
    map.computeIfAbsent(9, create).add(NamespacedKey.fromString("minecraft:iron_helmet"));
    map.computeIfAbsent(14, create).add(NamespacedKey.fromString("minecraft:iron_hoe"));
    map.computeIfAbsent(9, create).add(NamespacedKey.fromString("minecraft:iron_leggings"));
    map.computeIfAbsent(14, create).add(NamespacedKey.fromString("minecraft:iron_pickaxe"));
    map.computeIfAbsent(14, create).add(NamespacedKey.fromString("minecraft:iron_shovel"));
    map.computeIfAbsent(14, create).add(NamespacedKey.fromString("minecraft:iron_spear"));
    map.computeIfAbsent(14, create).add(NamespacedKey.fromString("minecraft:iron_sword"));
    map.computeIfAbsent(15, create).add(NamespacedKey.fromString("minecraft:leather_boots"));
    map.computeIfAbsent(15, create).add(NamespacedKey.fromString("minecraft:leather_chestplate"));
    map.computeIfAbsent(15, create).add(NamespacedKey.fromString("minecraft:leather_helmet"));
    map.computeIfAbsent(15, create).add(NamespacedKey.fromString("minecraft:leather_leggings"));
    map.computeIfAbsent(15, create).add(NamespacedKey.fromString("minecraft:mace"));
    map.computeIfAbsent(15, create).add(NamespacedKey.fromString("minecraft:netherite_axe"));
    map.computeIfAbsent(15, create).add(NamespacedKey.fromString("minecraft:netherite_boots"));
    map.computeIfAbsent(15, create).add(NamespacedKey.fromString("minecraft:netherite_chestplate"));
    map.computeIfAbsent(15, create).add(NamespacedKey.fromString("minecraft:netherite_helmet"));
    map.computeIfAbsent(15, create).add(NamespacedKey.fromString("minecraft:netherite_hoe"));
    map.computeIfAbsent(15, create).add(NamespacedKey.fromString("minecraft:netherite_leggings"));
    map.computeIfAbsent(15, create).add(NamespacedKey.fromString("minecraft:netherite_pickaxe"));
    map.computeIfAbsent(15, create).add(NamespacedKey.fromString("minecraft:netherite_shovel"));
    map.computeIfAbsent(15, create).add(NamespacedKey.fromString("minecraft:netherite_spear"));
    map.computeIfAbsent(15, create).add(NamespacedKey.fromString("minecraft:netherite_sword"));
    map.computeIfAbsent(5, create).add(NamespacedKey.fromString("minecraft:stone_axe"));
    map.computeIfAbsent(5, create).add(NamespacedKey.fromString("minecraft:stone_hoe"));
    map.computeIfAbsent(5, create).add(NamespacedKey.fromString("minecraft:stone_pickaxe"));
    map.computeIfAbsent(5, create).add(NamespacedKey.fromString("minecraft:stone_shovel"));
    map.computeIfAbsent(5, create).add(NamespacedKey.fromString("minecraft:stone_spear"));
    map.computeIfAbsent(5, create).add(NamespacedKey.fromString("minecraft:stone_sword"));
    map.computeIfAbsent(1, create).add(NamespacedKey.fromString("minecraft:trident"));
    map.computeIfAbsent(9, create).add(NamespacedKey.fromString("minecraft:turtle_helmet"));
    map.computeIfAbsent(15, create).add(NamespacedKey.fromString("minecraft:wooden_axe"));
    map.computeIfAbsent(15, create).add(NamespacedKey.fromString("minecraft:wooden_hoe"));
    map.computeIfAbsent(15, create).add(NamespacedKey.fromString("minecraft:wooden_pickaxe"));
    map.computeIfAbsent(15, create).add(NamespacedKey.fromString("minecraft:wooden_shovel"));
    map.computeIfAbsent(15, create).add(NamespacedKey.fromString("minecraft:wooden_spear"));
    map.computeIfAbsent(15, create).add(NamespacedKey.fromString("minecraft:wooden_sword"));
    // </editor-fold>
    return map;
  }

}
