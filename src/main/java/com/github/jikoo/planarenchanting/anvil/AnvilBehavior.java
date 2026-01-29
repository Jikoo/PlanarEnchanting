package com.github.jikoo.planarenchanting.anvil;

import com.github.jikoo.planarenchanting.util.MetaCachedStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

public interface AnvilBehavior {

  static AnvilBehavior VANILLA = new AnvilBehavior() {};

  /**
   * Get whether an {@link Enchantment} is applicable for a wrapped {@link ItemStack}.
   *
   * @param enchantment the {@code Enchantment} to check for applicability
   * @param base the item that may be enchanted
   * @return whether the {@code Enchantment} can be applied
   */
  default boolean enchantApplies(@NotNull Enchantment enchantment, @NotNull MetaCachedStack base) {
    return enchantment.canEnchantItem(base.getItem());
  }

  /**
   * Get whether two {@link Enchantment Enchantments} conflict.
   *
   * @return whether the {@code Enchantments} conflict
   */
  default boolean enchantsConflict(@NotNull Enchantment enchant1, @NotNull Enchantment enchant2) {
    return enchant1.conflictsWith(enchant2);
  }

  /**
   * Get the maximum level for an {@link Enchantment}.
   *
   * @return the maximum level for an {@code Enchantment}
   */
  default int getEnchantMaxLevel(@NotNull Enchantment enchantment) {
    return enchantment.getMaxLevel();
  }

  /**
   * Get whether an item should combine its {@link Enchantment Enchantments} with another item.
   *
   * @param base the base item
   * @param addition the item added
   * @return whether items should combine {@code Enchantments}
   */
  default boolean itemsCombineEnchants(@NotNull MetaCachedStack base, @NotNull MetaCachedStack addition) {
    ItemType additionType = addition.getItem().getType().asItemType();
    return base.getItem().getType().asItemType() == additionType || additionType == ItemType.ENCHANTED_BOOK;
  }

  /**
   * Get whether an item is repaired by another item. This is not the same as a repair via
   * combination of like items! Like items always attempt to combine durability. If you require
   * different behavior, override {@link Anvil#getResult(org.bukkit.inventory.view.AnvilView)}
   * and do not call {@link AnvilFunctions#REPAIR_WITH_COMBINATION}.
   *
   * @param repaired the item repaired
   * @param repairMat the item used to repair
   * @return the method determining whether an item is repaired by another item
   */
  default boolean itemRepairedBy(@NotNull MetaCachedStack repaired, @NotNull MetaCachedStack repairMat) {
    return RepairMaterial.repairs(repaired.getItem(), repairMat.getItem());
  }

}
