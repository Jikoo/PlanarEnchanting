package com.github.jikoo.planarenchanting.anvil;

import org.bukkit.enchantments.Enchantment;
import org.jspecify.annotations.NullMarked;

/**
 * Defines areas that are most likely to be modified for convenient {@link Anvil} manipulation.
 *
 * @param <T> the type of the input items
 */
@NullMarked
public interface AnvilBehavior<T> {

  /**
   * Get whether an {@link Enchantment} is applicable for an item.
   *
   * @param enchantment the {@code Enchantment} to check for applicability
   * @param base the item that may be enchanted
   * @return whether the {@code Enchantment} can be applied
   */
  boolean enchantApplies(Enchantment enchantment, T base);

  /**
   * Get whether two {@link Enchantment Enchantments} conflict.
   *
   * @return whether the {@code Enchantments} conflict
   */
  default boolean enchantsConflict(Enchantment enchant1, Enchantment enchant2) {
    return enchant1.conflictsWith(enchant2);
  }

  /**
   * Get the maximum level for an {@link Enchantment}.
   *
   * @return the maximum level for an {@code Enchantment}
   */
  default int getEnchantMaxLevel(Enchantment enchantment) {
    return enchantment.getMaxLevel();
  }

  /**
   * Get whether an item should combine its {@link Enchantment Enchantments} with another item.
   *
   * @param base the base item
   * @param addition the item added
   * @return whether items should combine {@code Enchantments}
   */
  boolean itemsCombineEnchants(T base, T addition);

  /**
   * Get whether an item is repaired by another item. This is not the same as a repair via
   * combination of like items! Like items always attempt to combine durability unless otherwise
   * specified when calculating anvil result.
   *
   * @param repaired the item repaired
   * @param repairMat the item used to repair
   * @return the method determining whether an item is repaired by another item
   */
  boolean itemRepairedBy(T repaired, T repairMat);

}
