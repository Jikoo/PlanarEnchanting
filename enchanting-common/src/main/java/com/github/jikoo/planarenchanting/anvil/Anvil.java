package com.github.jikoo.planarenchanting.anvil;

import org.bukkit.inventory.view.AnvilView;

/**
 * Defines behavior for calculating a result based on the state of an anvil.
 */
public interface Anvil {

  /**
   * Produce an {@link AnvilResult} for an anvil based on its inputs.
   *
   * @param view the {@link AnvilView} to calculate a result for
   * @return the {@code AnvilResult} produced
   */
  AnvilResult getResult(AnvilView view);

}
