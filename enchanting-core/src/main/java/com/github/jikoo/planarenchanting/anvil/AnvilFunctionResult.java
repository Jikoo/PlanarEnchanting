package com.github.jikoo.planarenchanting.anvil;

import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

/**
 * The result of an {@link AnvilFunction}. Used to modify operation state.
 */
public interface AnvilFunctionResult {

  /** Constant representing a result that does nothing. */
  AnvilFunctionResult EMPTY = new AnvilFunctionResult() {};

  /**
   * Get the number of additional levels this function will cost to perform.
   *
   * @return the number of levels to add to the total cost
   */
  default int getLevelCostIncrease() {
    return 0;
  }

  /**
   * Get the number of additional items to consume from the secondary input slot. Note that
   * {@code 0} and {@code 1} yield the same result - the added item is consumed either way.
   *
   * @return the number of items to consume from the secondary input slot
   */
  default int getMaterialCostIncrease() {
    return 0;
  }

  /**
   * Modify the given {@link ItemMeta} to reflect the changes applied by a function.
   *
   * @param itemMeta the {@code ItemMeta} to modify
   */
  default void modifyResult(@Nullable ItemMeta itemMeta) {}

}
