package com.github.jikoo.planarenchanting.anvil;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.jspecify.annotations.NullMarked;

/**
 * A {@link Temperer} for {@link ItemMeta}-based operations.
 */
@NullMarked
public class MetaTemperer implements Temperer<MetaCachedStack> {

  public static final MetaTemperer INSTANCE = new MetaTemperer();

  @Override
  public boolean hasChanged(
      MetaCachedStack base,
      MetaCachedStack addition,
      MetaCachedStack result
  ) {
    ItemMeta baseMeta = base.getMeta();
    ItemMeta resultMeta = result.getMeta();

    // If the base or the result has no meta, it is empty.
    if (baseMeta == null || resultMeta == null) {
      return false;
    }

    // Reset certain characteristics when verifying that changes have actually been performed.
    resultMeta = resultMeta.clone();

    // Ignore repair cost changes.
    if (baseMeta instanceof Repairable baseRepairable
        && resultMeta instanceof Repairable resultRepairable) {
      resultRepairable.setRepairCost(baseRepairable.getRepairCost());
    }
    // Ignore name changes if addition is not empty.
    if (addition.getItem().getType() != Material.AIR) {
      resultMeta.setDisplayName(baseMeta.getDisplayName());
    }

    // If reset meta is not identical then an operation is occurring.
    // Note that meta must be compared via ItemFactory#equals(ItemMeta, ItemMeta) for test purposes.
    return !Bukkit.getItemFactory().equals(baseMeta, resultMeta);
  }

  @Override
  public ItemStack temper(MetaCachedStack result) {
    result.getItem().setItemMeta(result.getMeta());
    return result.getItem();
  }

  private MetaTemperer() {}

}
