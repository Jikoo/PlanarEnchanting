package com.github.jikoo.planarenchanting.anvil;

import com.github.jikoo.planarenchanting.util.ItemUtil;
import com.github.jikoo.planarenchanting.util.MetaCachedStack;
import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.view.AnvilView;
import org.jetbrains.annotations.NotNull;

/**
 * A mutable data holder similar to an {@link AnvilResult}.
 */
public class AnvilOperationState {

  private final @NotNull AnvilOperation operation;
  private final @NotNull AnvilView view;
  private final @NotNull MetaCachedStack base;
  private final @NotNull MetaCachedStack addition;
  protected final @NotNull MetaCachedStack result;
  private int levelCost = 0;
  private int materialCost = 0;

  /**
   * Create an {@code AnvilOperationState} instance for the given operation and inventory.
   *
   * @param operation the {@link AnvilOperation} mutating the state
   * @param view the {@link AnvilView} the state is derived from
   */
  public AnvilOperationState(@NotNull AnvilOperation operation, @NotNull AnvilView view) {
    this.operation = operation;
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

  /**
   * Attempt to apply the given {@link AnvilFunction}, modifying the result and costs as necessary.
   * Note that a function reporting itself applicable does not guarantee that the result or costs
   * will actually differ.
   *
   * @see AnvilFunction#canApply(AnvilOperation, AnvilOperationState)
   * @param function the {@code AnvilFunction} to apply
   * @return whether the {@link AnvilFunction} could apply
   */
  public boolean apply(@NotNull AnvilFunction function) {
    if (!function.canApply(this.operation, this)) {
      return false;
    }

    AnvilFunctionResult anvilResult = function.getResult(this.operation, this);

    anvilResult.modifyResult(this.result.getMeta());
    this.levelCost += anvilResult.getLevelCostIncrease();
    this.materialCost += anvilResult.getMaterialCostIncrease();

    return true;
  }

  /**
   * Finalize the {@code AnvilOperationState} into an {@link AnvilResult}.
   *
   * @return the finalized result
   */
  public AnvilResult forge() {

    ItemMeta baseMeta = this.getBase().getMeta();
    ItemMeta resultMeta = this.result.getMeta();

    // If base meta is null, it is empty. No input = no output.
    if (baseMeta == null) {
      return AnvilResult.EMPTY;
    }

    // If result meta is null, result is empty.
    if (resultMeta == null) {
      return AnvilResult.EMPTY;
    }

    // Update result meta.
    this.result.getItem().setItemMeta(resultMeta);

    // Reset result meta to base meta to ignore certain characteristics when verifying that
    // changes have actually been performed.
    resultMeta = resultMeta.clone();

    // Ignore repair cost changes.
    if (baseMeta instanceof Repairable baseRepairable
        && resultMeta instanceof Repairable resultRepairable) {
      resultRepairable.setRepairCost(baseRepairable.getRepairCost());
    }
    // Ignore name changes if addition is not empty.
    if (!ItemUtil.isEmpty(this.addition.getItem())) {
      resultMeta.setDisplayName(baseMeta.getDisplayName());
    }

    // If reset meta is identical then no operation is occurring.
    // Note that meta must be compared via ItemFactory#equals(ItemMeta, ItemMeta) for test purposes.
    if (Bukkit.getItemFactory().equals(baseMeta, resultMeta)) {
      return AnvilResult.EMPTY;
    }

    return new AnvilResult(this.result.getItem(), this.levelCost, this.materialCost);
  }

}
