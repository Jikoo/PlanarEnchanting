package com.github.jikoo.planarenchanting.util.mock.enchantments;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

// TODO remove
public class EnchantmentHolder extends Enchantment {

  private final NamespacedKey key;
  private final int maxLevel;
  private final @NotNull EnchantmentTarget target;
  private final boolean treasure;
  private final boolean curse;
  private final @NotNull Collection<Enchantment> conflicts;

  public EnchantmentHolder(
      @NotNull NamespacedKey key,
      int maxLevel,
      @NotNull EnchantmentTarget target,
      boolean treasure,
      boolean curse,
      @NotNull Collection<Enchantment> conflicts) {
    this.key = key;
    this.maxLevel = maxLevel;
    this.target = target;
    this.treasure = treasure;
    this.curse = curse;
    this.conflicts = conflicts;
  }

  @Override
  public @NotNull NamespacedKey getKey() {
    return key;
  }

  @Deprecated
  @Override
  public @NotNull String getName() {
    return key.getKey();
  }

  @Override
  public int getMaxLevel() {
    return maxLevel;
  }

  @Override
  public int getStartLevel() {
    return 1;
  }

  @Override
  public @NotNull EnchantmentTarget getItemTarget() {
    return target;
  }

  @Deprecated
  @Override
  public boolean isTreasure() {
    return treasure;
  }

  @Deprecated
  @Override
  public boolean isCursed() {
    return curse;
  }

  @Override
  public boolean conflictsWith(@NotNull Enchantment other) {
    return conflicts.contains(other);
  }

  @Override
  public boolean canEnchantItem(@NotNull ItemStack item) {
    return getItemTarget().includes(item);
  }
}
