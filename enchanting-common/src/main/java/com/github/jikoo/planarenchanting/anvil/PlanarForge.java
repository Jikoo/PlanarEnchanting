package com.github.jikoo.planarenchanting.anvil;

import java.util.function.Function;
import org.bukkit.Material;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;
import org.jspecify.annotations.NullMarked;

/**
 * The default {@link Anvil} implementation. Wraps a {@link WorkPiece}, {@link AnvilBehavior},
 * and {@link AnvilFunctionsProvider} to create an {@link AnvilResult} with vanilla parity.
 *
 * @param <T> the type of the input and output items
 */
@NullMarked
public final class PlanarForge<T> implements Anvil {

  private final Function<AnvilView, WorkPiece<T>> createPiece;
  private final AnvilBehavior<T> behavior;
  private final AnvilFunctionsProvider<T> functions;

  PlanarForge(
      Function<AnvilView, WorkPiece<T>> createPiece,
      AnvilBehavior<T> behavior,
      AnvilFunctionsProvider<T> functions
  ) {
    this.createPiece = createPiece;
    this.behavior = behavior;
    this.functions = functions;
  }

  @Override
  public AnvilResult getResult(AnvilView view) {
    WorkPiece<T> piece = createPiece.apply(view);

    AnvilInventory anvil = view.getTopInventory();
    ItemStack base = anvil.getItem(0);
    if (base == null || base.getType() == Material.AIR) { // TODO check count > 0 too in these areas?
      return AnvilResult.EMPTY;
    }

    // Apply base cost.
    piece.apply(behavior, functions.addPriorWorkLevelCost()); // TODO check if renames ignore this now

    ItemStack addition = anvil.getItem(1);
    if (addition == null || addition.getType() == Material.AIR) {
      if (!piece.apply(behavior, functions.rename())) {
        // If there isn't a rename occurring, nothing is happening.
        return AnvilResult.EMPTY;
      }

      // If the only thing occurring is a renaming operation, it is always allowed.
      piece.setLevelCost(Math.min(piece.getLevelCost(), view.getMaximumRepairCost() - 1));

      // No addition means no other operations to perform.
      return piece.temper();
    }

    if (base.getAmount() != 1) {
      // Multi-renames are allowed, multi-modifications are not.
      // Vanilla allows multi-modifications "for creative" but the way it does it is problematic.
      return AnvilResult.EMPTY;
    }

    piece.apply(behavior, functions.rename());
    // Apply prior work cost after rename.
    // Rename also applies a prior work cost but does not increase it.
    piece.apply(behavior, functions.setItemPriorWork());

    if (!piece.apply(behavior, functions.repairWithMaterial())) {
      // Only do combination repair if this is not a material repair.
      piece.apply(behavior, functions.repairWithCombine());
    }

    // Differing from vanilla - since we use a custom determination for whether enchantments should
    // transfer (which defaults to indirectly mimicking vanilla), enchantments may need to be
    // applied from a material repair.
    piece.apply(behavior, functions.combineEnchantsJava());

    return piece.temper();
  }

}
