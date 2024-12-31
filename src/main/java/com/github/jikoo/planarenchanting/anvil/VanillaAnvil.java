package com.github.jikoo.planarenchanting.anvil;

import com.github.jikoo.planarenchanting.util.ItemUtil;
import org.bukkit.inventory.view.AnvilView;
import org.jetbrains.annotations.NotNull;

public class VanillaAnvil {

  private final @NotNull AnvilBehavior behavior;

  public VanillaAnvil() {
    this(AnvilBehavior.VANILLA);
  }

  public VanillaAnvil(@NotNull AnvilBehavior behavior) {
    this.behavior = behavior;
  }

  /**
   * Get an {@link AnvilResult} for this anvil operation.
   *
   * @return the {@code AnvilResult}
   */
  public @NotNull AnvilResult getResult(@NotNull AnvilView view) {
    AnvilState state = new AnvilState(behavior, view);

    if (ItemUtil.isEmpty(state.getBase().getItem())) {
      return AnvilResult.EMPTY;
    }

    // Apply base cost.
    state.apply(AnvilFunctions.PRIOR_WORK_LEVEL_COST);

    if (ItemUtil.isEmpty(state.getAddition().getItem())) {
      if (state.apply(AnvilFunctions.RENAME)) {
        // If the only thing occurring is a renaming operation, it is always allowed.
        state.setLevelCost(Math.min(state.getLevelCost(), state.getAnvil().getMaximumRepairCost() - 1));
      }

      // No addition means no additional operations to perform.
      return state.forge();
    }


    if (state.getBase().getItem().getAmount() != 1) {
      // Multi-renames are allowed, multi-modifications are not.
      // Vanilla allows multi-modifications "for creative" but the way it does it is problematic.
      return AnvilResult.EMPTY;
    }

    state.apply(AnvilFunctions.RENAME);
    // Apply prior work cost after rename.
    // Rename also applies a prior work cost but does not increase it.
    state.apply(AnvilFunctions.UPDATE_PRIOR_WORK_COST);

    if (!state.apply(AnvilFunctions.REPAIR_WITH_MATERIAL)) {
      // Only do combination repair if this is not a material repair.
      state.apply(AnvilFunctions.REPAIR_WITH_COMBINATION);
    }

    // Differing from vanilla - since we use a custom determination for whether enchantments should
    // transfer (which defaults to indirectly mimicking vanilla), enchantments may need to be
    // applied from a material repair.
    state.apply(AnvilFunctions.COMBINE_ENCHANTMENTS_JAVA_EDITION);

    return state.forge();
  }

}
