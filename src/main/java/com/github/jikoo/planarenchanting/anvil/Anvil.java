package com.github.jikoo.planarenchanting.anvil;

import com.github.jikoo.planarenchanting.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.view.AnvilView;
import org.jetbrains.annotations.NotNull;

public class Anvil {

  private final @NotNull AnvilBehavior behavior;

  public Anvil() {
    this(AnvilBehavior.VANILLA);
  }

  public Anvil(@NotNull AnvilBehavior behavior) {
    this.behavior = behavior;
  }

  /**
   * Get an {@link AnvilResult} for this anvil operation.
   *
   * @return the {@code AnvilResult}
   */
  public @NotNull AnvilResult getResult(@NotNull AnvilView view) {
    AnvilState state = new AnvilState(view);

    if (ItemUtil.isEmpty(state.getBase().getItem())) {
      return AnvilResult.EMPTY;
    }

    // Apply base cost.
    apply(state, AnvilFunctions.PRIOR_WORK_LEVEL_COST);

    if (ItemUtil.isEmpty(state.getAddition().getItem())) {
      if (apply(state, AnvilFunctions.RENAME)) {
        // If the only thing occurring is a renaming operation, it is always allowed.
        state.setLevelCost(Math.min(state.getLevelCost(), state.getAnvil().getMaximumRepairCost() - 1));
      }

      // No addition means no additional operations to perform.
      return forge(state);
    }


    if (state.getBase().getItem().getAmount() != 1) {
      // Multi-renames are allowed, multi-modifications are not.
      // Vanilla allows multi-modifications "for creative" but the way it does it is problematic.
      return AnvilResult.EMPTY;
    }

    apply(state, AnvilFunctions.RENAME);
    // Apply prior work cost after rename.
    // Rename also applies a prior work cost but does not increase it.
    apply(state, AnvilFunctions.UPDATE_PRIOR_WORK_COST);

    if (!apply(state, AnvilFunctions.REPAIR_WITH_MATERIAL)) {
      // Only do combination repair if this is not a material repair.
      apply(state, AnvilFunctions.REPAIR_WITH_COMBINATION);
    }

    // Differing from vanilla - since we use a custom determination for whether enchantments should
    // transfer (which defaults to indirectly mimicking vanilla), enchantments may need to be
    // applied from a material repair.
    apply(state, AnvilFunctions.COMBINE_ENCHANTMENTS_JAVA_EDITION);

    return forge(state);
  }

  /**
   * Attempt to apply the given {@link AnvilFunction}, modifying the result and costs as necessary.
   * Note that a function reporting itself applicable does not guarantee that the result or costs
   * will actually differ.
   *
   * @see AnvilFunction#canApply(AnvilBehavior, AnvilState)
   * @param function the {@code AnvilFunction} to apply
   * @return whether the {@link AnvilFunction} could apply
   */
  protected final boolean apply(@NotNull AnvilState state, @NotNull AnvilFunction function) {
    if (!function.canApply(this.behavior, state)) {
      return false;
    }

    AnvilFunctionResult anvilResult = function.getResult(this.behavior, state);

    anvilResult.modifyResult(state.result.getMeta());
    state.setLevelCost(state.getLevelCost() + anvilResult.getLevelCostIncrease());
    state.setMaterialCost(state.getMaterialCost() + anvilResult.getMaterialCostIncrease());

    return true;
  }

  /**
   * Finalize the {@code AnvilOperationState} into an {@link AnvilResult}.
   *
   * @return the finalized result
   */
  protected final AnvilResult forge(@NotNull AnvilState state) {

    ItemMeta baseMeta = state.getBase().getMeta();
    ItemMeta resultMeta = state.result.getMeta();

    // If base meta is null, it is empty. No input = no output.
    if (baseMeta == null) {
      return AnvilResult.EMPTY;
    }

    // If result meta is null, result is empty.
    if (resultMeta == null) {
      return AnvilResult.EMPTY;
    }

    // Update result meta.
    state.result.getItem().setItemMeta(resultMeta);

    // Reset result meta to base meta to ignore certain characteristics when verifying that
    // changes have actually been performed.
    resultMeta = resultMeta.clone();

    // Ignore repair cost changes.
    if (baseMeta instanceof Repairable baseRepairable
        && resultMeta instanceof Repairable resultRepairable) {
      resultRepairable.setRepairCost(baseRepairable.getRepairCost());
    }
    // Ignore name changes if addition is not empty.
    if (!ItemUtil.isEmpty(state.getAddition().getItem())) {
      resultMeta.customName(baseMeta.customName());
    }

    // If reset meta is identical then no operation is occurring.
    // Note that meta must be compared via ItemFactory#equals(ItemMeta, ItemMeta) for test purposes.
    if (Bukkit.getItemFactory().equals(baseMeta, resultMeta)) {
      return AnvilResult.EMPTY;
    }

    return new AnvilResult(state.result.getItem(), state.getLevelCost(), state.getMaterialCost());
  }

}
