package com.github.jikoo.planarenchanting.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A collection of item-related utility functions.
 */
public final class ItemUtil {

  /** Constant for air itemstacks. */
  public static final ItemStack AIR = new ItemStack(Material.AIR) {
    @Override
    public void setType(@NotNull Material type) {
      throw new UnsupportedOperationException("Cannot modify AIR constant.");
    }
  };

  /**
   * Check if an {@link ItemStack} is empty.
   *
   * @param itemStack the item
   * @return whether the item is empty
   */
  @Contract("null -> true")
  public static boolean isEmpty(@Nullable ItemStack itemStack) {
    return itemStack == null || itemStack.getType() == Material.AIR || itemStack.getAmount() < 1;
  }

  /**
   * Get the repair cost for a {@link CachedValue} of an {@link ItemMeta}, falling through to 0.
   *
   * @param meta the cached value
   * @return the repair cost
   */
  public static int getRepairCost(@Nullable ItemMeta meta) {
    if (meta instanceof Repairable repairable) {
      return repairable.getRepairCost();
    }

    return 0;
  }

  private ItemUtil() {
    throw new IllegalStateException("Cannot instantiate static helper method container.");
  }

}
