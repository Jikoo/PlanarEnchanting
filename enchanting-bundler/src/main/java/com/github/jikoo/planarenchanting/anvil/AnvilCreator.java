package com.github.jikoo.planarenchanting.anvil;

import com.github.jikoo.planarenchanting.util.ServerCapabilities;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;
import org.jspecify.annotations.NullMarked;

/**
 * Accessor for creating an appropriate {@link Anvil} for the platform.
 */
@NullMarked
public final class AnvilCreator {

  /**
   * Create a new platform-dependent {@link Anvil}. It will use vanilla-style behavior to produce
   * results.
   *
   * @return the anvil implementation
   */
  public static Anvil create() {
    if (ServerCapabilities.DATA_COMPONENT) {
      return new PlanarForge<>(AnvilCreator::createComponentPiece, new ComponentVanillaBehavior(), ComponentAnvilFunctions.INSTANCE);
    } else {
      return new PlanarForge<>(AnvilCreator::createMetaPiece, new MetaVanillaBehavior(), MetaAnvilFunctions.INSTANCE);
    }
  }

  /**
   * Create a new anvil {@link WorkPiece} based on Paper's {@code DataComponent}.
   *
   * @param view the AnvilView to operate on
   * @return the resulting work piece
   * @see ServerCapabilities#DATA_COMPONENT
   */
  public static WorkPiece<ItemStack> createComponentPiece(AnvilView view) {
    return new WorkPiece<>(new ComponentViewState(view), ComponentTemperer.INSTANCE);
  }

  /**
   * Create a new anvil {@link WorkPiece} based on the Bukkit API.
   *
   * <p>Note that this relies on methods which are now deprecated in Paper. It is advisable to use
   * {@link #createComponentPiece(AnvilView)} instead if possible.</p>
   *
   * <p>To prevent creation of numerous duplicate copies of the item's
   * {@link org.bukkit.inventory.meta.ItemMeta}, operations are performed on a
   * {@link MetaCachedStack}. Changes are only applied to the result when the piece is tempered.</p>
   *
   * @param view the AnvilView to operate on
   * @return the resulting work piece
   * @see #createComponentPiece(AnvilView)
   * @see ServerCapabilities#DATA_COMPONENT
   */
  public static WorkPiece<MetaCachedStack> createMetaPiece(AnvilView view) {
    return new WorkPiece<>(new MetaViewState(view), MetaTemperer.INSTANCE);
  }

  private AnvilCreator() {}

}
