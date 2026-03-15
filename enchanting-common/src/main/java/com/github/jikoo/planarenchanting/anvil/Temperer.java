package com.github.jikoo.planarenchanting.anvil;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

/**
 * Defines behavior required to finalize a workable item into an {@link ItemStack}.
 *
 * @param <T> the type of the input and output items
 */
@NullMarked
public interface Temperer<T> {

  /**
   * Check if an object has been changed while being worked in the anvil.
   *
   * @param base the original object
   * @param addition the object being added to the base
   * @param result the current result state
   * @return true if the result is meaningfully different
   */
  boolean hasChanged(T base, T addition, T result);

  /**
   * Transform the working result into a finalized ItemStack.
   *
   * @param result the current result state
   * @return the result as an ItemStack
   */
  ItemStack temper(T result);

}
