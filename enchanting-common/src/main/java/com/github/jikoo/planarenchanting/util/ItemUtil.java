package com.github.jikoo.planarenchanting.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;

/**
 * A collection of item-related utility functions.
 */
@NullMarked
public final class ItemUtil {

  /** Constant for air itemstacks. */
  public static final ItemStack AIR = new ItemStack(Material.AIR) {
    /**
     * Don't do this. Make a new item instead.
     *
     * @throws UnsupportedOperationException when invoked
     * @deprecated Material for AIR constant cannot be changed.
     */
    @Contract(value = "_ -> fail", pure = true)
    @Deprecated(since = "added")
    @Override
    public void setType(Material type) {
      throw new UnsupportedOperationException("Cannot modify AIR constant.");
    }
  };

  private ItemUtil() {
    // TODO parity for static helpers
    throw new IllegalStateException("Cannot instantiate static helper container.");
  }

}
