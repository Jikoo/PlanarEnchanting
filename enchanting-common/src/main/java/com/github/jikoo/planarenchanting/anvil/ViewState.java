package com.github.jikoo.planarenchanting.anvil;

import org.bukkit.inventory.view.AnvilView;
import org.jspecify.annotations.NullMarked;

/**
 * A wrapper for an {@link AnvilView} transforming the inputs and output into the correct
 * implementation types.
 *
 * @param <T> the type of the input items
 */
@NullMarked
public interface ViewState<T> {

  /**
   * Get the {@link AnvilView} the state is derived from.
   *
   * @return the {@code AnvilView}
   */
  AnvilView getAnvilView();

  /**
   * Get the base input item.
   *
   * @return the base input item
   */
  T getBase();

  /**
   * Get the secondary input item.
   *
   * @return the secondary input item
   */
  T getAddition();

  /**
   * Create a result item copied from the base item.
   *
   * @return the result item
   */
  T createResult();

}
