package com.github.jikoo.planarenchanting.util.mock;

import be.seeseemelk.mockbukkit.enchantments.EnchantmentMock;
import java.util.function.IntUnaryOperator;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantment.Rarity;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

public class CraftEnchantMock extends EnchantmentMock {

  private final int rarityWeight;
  private final IntUnaryOperator minQuality;
  private final IntUnaryOperator maxQuality;

  public CraftEnchantMock(
      @NotNull org.bukkit.enchantments.Enchantment enchantment,
      int rarityWeight,
      @NotNull IntUnaryOperator minQuality,
      @NotNull IntUnaryOperator maxQuality) {
    this(enchantment.getKey(), rarityWeight, minQuality, maxQuality);
  }

  public CraftEnchantMock(
      @NotNull NamespacedKey key,
      int rarityWeight,
      @NotNull IntUnaryOperator minQuality,
      @NotNull IntUnaryOperator maxQuality) {
    super(key, key.getNamespace());
    this.rarityWeight = rarityWeight;
    this.minQuality = minQuality;
    this.maxQuality = maxQuality;
  }

  public Enchantment getHandle() {
    return new Enchantment(minQuality, maxQuality, new Rarity(rarityWeight));
  }

}
