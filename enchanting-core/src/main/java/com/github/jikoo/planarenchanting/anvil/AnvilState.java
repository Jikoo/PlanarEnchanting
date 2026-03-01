package com.github.jikoo.planarenchanting.anvil;

import com.github.jikoo.planarenchanting.util.MetaCachedStack;
import org.bukkit.inventory.view.AnvilView;
import org.jetbrains.annotations.NotNull;

/**
 * A mutable data holder similar to an {@link AnvilResult}.
 */
public class AnvilState {

  private final @NotNull AnvilView view;
  private final @NotNull MetaCachedStack base;
  private final @NotNull MetaCachedStack addition;
  final @NotNull MetaCachedStack result;
  private int levelCost = 0;
  private int materialCost = 0;

  /**
   * Create an {@code AnvilOperationState} instance for the given operation and inventory.
   *
   * @param view the {@link AnvilView} the state is derived from
   */
  public AnvilState(@NotNull AnvilView view) {
    this.view = view;
    this.base = new MetaCachedStack(this.view.getItem(0));
    this.addition = new MetaCachedStack(this.view.getItem(1));
    this.result = new MetaCachedStack(this.base.getItem().clone());
  }

  /**
   * Get the {@link AnvilView} the state is derived from.
   *
   * @return the {@code AnvilView}
   */
  public AnvilView getAnvil() {
    return this.view;
  }

  /**
   * Get the base input item from the {@link AnvilView}.
   *
   * @return the base input item
   */
  public @NotNull MetaCachedStack getBase() {
    return this.base;
  }

  /**
   * Get the secondary input item from the {@link AnvilView}.
   *
   * @return the secondary input item
   */
  public @NotNull MetaCachedStack getAddition() {
    return this.addition;
  }

  /**
   * Get the number of levels to be consumed by the operation.
   *
   * @return the number of levels to be consumed by the operation
   */
  public int getLevelCost() {
    return this.levelCost;
  }

  /**
   * Set the number of levels to be consumed by the operation.
   *
   * @param levelCost the number of levels to be consumed by the operation
   */
  public void setLevelCost(int levelCost) {
    this.levelCost = levelCost;
  }

  /**
   * Get the amount of items to be consumed from the addition slot.
   *
   * @return the amount of items to be consumed from the addition slot
   */
  public int getMaterialCost() {
    return this.materialCost;
  }

  /**
   * Set the amount of items to be consumed from the addition slot.
   *
   * @param materialCost the amount of items to be consumed from the addition slot
   */
  public void setMaterialCost(int materialCost) {
    this.materialCost = materialCost;
  }

}
