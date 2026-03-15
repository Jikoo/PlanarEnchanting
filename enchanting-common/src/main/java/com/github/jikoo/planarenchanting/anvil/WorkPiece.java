package com.github.jikoo.planarenchanting.anvil;

import org.bukkit.inventory.view.AnvilView;
import org.jspecify.annotations.NullMarked;

/**
 * A work-in-progress anvil result.
 *
 * @param <T> the type of the input and output items
 */
@NullMarked
public final class WorkPiece<T> {

  private final ViewState<T> view;
  private final Temperer<T> temperer;
  final T result;
  private int levelCost = 0;
  private int materialCost = 0;

  /**
   * Create a {@code WorkPiece} instance for the given operation and inventory.
   *
   * @param state the {@link ViewState} the state is derived from
   */
  public WorkPiece(ViewState<T> state, Temperer<T> temperer) {
    this.view = state;
    this.temperer = temperer;
    this.result = view.createResult();
  }

  /**
   * Get the base input item from the {@link AnvilView}.
   *
   * @return the base input item
   */
  public T getBase() {
    return view.getBase();
  }

  /**
   * Get the secondary input item from the {@link AnvilView}.
   *
   * @return the secondary input item
   */
  public T getAddition() {
    return view.getAddition();
  }

  /**
   * Get the number of levels to be consumed by the operation.
   *
   * @return the number of levels to be consumed by the operation
   */
  public int getLevelCost() {
    return levelCost;
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
    return materialCost;
  }

  /**
   * Set the amount of items to be consumed from the addition slot.
   *
   * @param materialCost the amount of items to be consumed from the addition slot
   */
  public void setMaterialCost(int materialCost) {
    this.materialCost = materialCost;
  }

  /**
   * Attempt to apply the given {@link AnvilFunction}, modifying the result and costs as necessary.
   * Note that a function reporting itself applicable does not guarantee that the result or costs
   * will actually differ.
   *
   * @see AnvilFunction#canApply(AnvilBehavior, ViewState, T)
   * @param function the {@code AnvilFunction} to apply
   * @return whether the {@link AnvilFunction} could apply
   */
  public boolean apply(AnvilBehavior<T> behavior, AnvilFunction<T> function) {
    if (!function.canApply(behavior, view, result)) {
      return false;
    }

    AnvilFunctionResult<T> anvilResult = function.getResult(behavior, view, result);

    anvilResult.modifyResult(result);
    setLevelCost(getLevelCost() + anvilResult.getLevelCostIncrease());
    setMaterialCost(getMaterialCost() + anvilResult.getMaterialCostIncrease());

    return true;
  }

  /**
   * Finalize the piece, applying any changes required to produce an {@link AnvilResult}.
   *
   * @return the finalized result
   */
  public AnvilResult temper() {
    if (temperer.hasChanged(view.getBase(), view.getAddition(), result)) {
      return new AnvilResult(temperer.temper(result), levelCost, materialCost);
    }
    return AnvilResult.EMPTY;
  }

}
