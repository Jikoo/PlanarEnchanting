package com.github.jikoo.planarenchanting.anvil;

import com.github.jikoo.planarenchanting.util.MetaCachedStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface AnvilBehavior {

  /**
   * Get whether an {@link Enchantment} is applicable for a wrapped {@link ItemStack}.
   *
   * @param enchantment the {@code Enchantment} to check for applicability
   * @param base the item that may be enchanted
   * @return whether the {@code Enchantment} can be applied
   */
  boolean enchantApplies(@NotNull Enchantment enchantment, @NotNull MetaCachedStack base);

  /**
   * Get whether two {@link Enchantment Enchantments} conflict.
   *
   * @return whether the {@code Enchantments} conflict
   */
  boolean enchantsConflict(@NotNull Enchantment enchant1, @NotNull Enchantment enchant2);

  /**
   * Get the maximum level for an {@link Enchantment}.
   *
   * @return the maximum level for an {@code Enchantment}
   */
  int getEnchantMaxLevel(@NotNull Enchantment enchantment);

  /**
   * Get whether an item should combine its {@link Enchantment Enchantments} with another item.
   *
   * @param base the base item
   * @param addition the item added
   * @return whether items should combine {@code Enchantments}
   */
  boolean itemsCombineEnchants(@NotNull MetaCachedStack base, @NotNull MetaCachedStack addition);

  /**
   * Get whether an item is repaired by another item. This is not the same as a repair via
   * combination of like items! Like items always attempt to combine durability. If you require
   * different behavior, override {@link VanillaAnvil#getResult(org.bukkit.inventory.view.AnvilView)}
   * and do not call {@link AnvilFunctions#REPAIR_WITH_COMBINATION}.
   *
   * @param repaired the item repaired
   * @param repairMat the item used to repair
   * @return the method determining whether an item is repaired by another item
   */
  public boolean itemRepairedBy(@NotNull MetaCachedStack repaired, @NotNull MetaCachedStack repairMat);

}
