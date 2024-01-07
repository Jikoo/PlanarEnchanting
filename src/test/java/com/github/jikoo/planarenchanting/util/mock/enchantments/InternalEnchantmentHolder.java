package com.github.jikoo.planarenchanting.util.mock.enchantments;

import java.util.List;
import java.util.function.IntUnaryOperator;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantment.Rarity;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.EnchantmentTarget;
import org.jetbrains.annotations.NotNull;

// TODO make interface and mock single additional method
public class InternalEnchantmentHolder extends EnchantmentHolder {

  private final int rarityWeight;
  private final IntUnaryOperator minQuality;
  private final IntUnaryOperator maxQuality;

  public InternalEnchantmentHolder(
      @NotNull org.bukkit.enchantments.Enchantment enchantment,
      int rarityWeight,
      @NotNull IntUnaryOperator minQuality,
      @NotNull IntUnaryOperator maxQuality) {
    super(
        enchantment.getKey(),
        enchantment.getMaxLevel(),
        enchantment.getItemTarget(),
        enchantment.isTreasure(),
        enchantment.isCursed(),
        // This is hacky, but it feels better than blindly casting to EnchantmentHolder.
        // At least if I forget to initialize enchantments first this will just cause the conflicts to be empty.
        Registry.ENCHANTMENT.stream().filter(enchantment::conflictsWith).toList());
    this.rarityWeight = rarityWeight;
    this.minQuality = minQuality;
    this.maxQuality = maxQuality;
  }

  public InternalEnchantmentHolder(
      @NotNull NamespacedKey key,
      int rarityWeight,
      @NotNull IntUnaryOperator minQuality,
      @NotNull IntUnaryOperator maxQuality) {
    super(key, 1, EnchantmentTarget.VANISHABLE, true, true, List.of());
    this.rarityWeight = rarityWeight;
    this.minQuality = minQuality;
    this.maxQuality = maxQuality;
  }

  public Enchantment getHandle() {
    return new Enchantment(getKey(), minQuality, maxQuality, new Rarity(rarityWeight));
  }

}
