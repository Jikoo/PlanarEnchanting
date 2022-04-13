package com.github.jikoo.planarenchanting.anvil;

import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;
import com.github.jikoo.planarenchanting.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A container for data required to calculate an anvil combination.
 */
public class AnvilOperation {

  private @NotNull BiPredicate<@NotNull Enchantment, @NotNull ItemStack> enchantApplies;
  private @NotNull BiPredicate<@NotNull Enchantment, @NotNull Enchantment> enchantsConflict;
  private @NotNull ToIntFunction<@NotNull Enchantment> enchantMaxLevel;
  private @NotNull BiPredicate<@NotNull ItemStack, @NotNull ItemStack> itemRepairedBy;
  private @NotNull BiPredicate<@NotNull ItemStack, @NotNull ItemStack> itemsCombineEnchants;

  /**
   * Construct a new {@code AnvilOperation}.
   */
  public AnvilOperation() {
    this.enchantApplies = Enchantment::canEnchantItem;
    this.enchantsConflict = Enchantment::conflictsWith;
    this.enchantMaxLevel = Enchantment::getMaxLevel;
    this.itemRepairedBy = RepairMaterial::repairs;
    this.itemsCombineEnchants = (base, addition) ->
        base.getType() == addition.getType()
            || addition.getType() == Material.ENCHANTED_BOOK;
  }

  /**
   * Get whether an {@link Enchantment} is applicable for an {@link ItemStack}.
   *
   * @param enchantment the {@code Enchantment} to check for applicability
   * @param itemStack the item that may be enchanted
   * @return whether the {@code Enchantment} can be applied
   */
  public boolean enchantApplies(@NotNull Enchantment enchantment, @NotNull ItemStack itemStack) {
    return this.enchantApplies.test(enchantment, itemStack);
  }

  /**
   * Set the method for determining if an {@link Enchantment} is applicable for an
   * {@link ItemStack}.
   *
   * @param enchantApplies the method for determining if an {@code Enchantment} is applicable
   */
  public void setEnchantApplies(
      @NotNull BiPredicate<@NotNull Enchantment, @NotNull ItemStack> enchantApplies) {
    this.enchantApplies = enchantApplies;
  }

  /**
   * Get the method for determining if {@link Enchantment Enchantments} conflict.
   *
   * @return whether the {@code Enchantments} conflict
   */
  public boolean enchantsConflict(@NotNull Enchantment enchant1, @NotNull Enchantment enchant2) {
    return this.enchantsConflict.test(enchant1, enchant2);
  }

  /**
   * Set the method for determining if {@link Enchantment Enchantments} conflict.
   *
   * @param enchantsConflict the method for determining if {@code Enchantments} conflict
   */
  public void setEnchantsConflict(
      @NotNull BiPredicate<@NotNull Enchantment, @NotNull Enchantment> enchantsConflict) {
    this.enchantsConflict = enchantsConflict;
  }

  /**
   * Get the method supplying maximum level for an {@link Enchantment}.
   *
   * @return the method supplying maximum level for an {@code Enchantment}
   */
  public int getEnchantMaxLevel(@NotNull Enchantment enchantment) {
    return this.enchantMaxLevel.applyAsInt(enchantment);
  }

  /**
   * Set the method supplying maximum level for an {@link Enchantment}.
   *
   * @param enchantMaxLevel the method supplying maximum level for an {@code Enchantment}
   */
  public void setEnchantMaxLevel(@NotNull ToIntFunction<@NotNull Enchantment> enchantMaxLevel) {
    this.enchantMaxLevel = enchantMaxLevel;
  }

  /**
   * Get whether an item should combine its {@link Enchantment Enchantments} with another item.
   *
   * @param base the base item
   * @param addition the item added
   * @return whether items should combine {@code Enchantments}
   */
  public boolean itemsCombineEnchants(@NotNull ItemStack base, @NotNull ItemStack addition) {
    return this.itemsCombineEnchants.test(base, addition);
  }

  /**
   * Set the method determining whether an item should combine its {@link Enchantment Enchantments}
   * with another item.
   *
   * @param itemsCombineEnchants the method determining whether an item should combine its
   *                         {@code Enchantments}
   */
  public void setItemsCombineEnchants(
      @NotNull BiPredicate<@NotNull ItemStack, @NotNull ItemStack> itemsCombineEnchants) {
    this.itemsCombineEnchants = itemsCombineEnchants;
  }

  /**
   * Get whether an item is repaired by another item.
   *
   * @see #setItemRepairedBy
   * @param repaired the item repaired
   * @param repairMat the item used to repair
   * @return the method determining whether an item is repaired by another item
   */
  public boolean itemRepairedBy(@NotNull ItemStack repaired, @NotNull ItemStack repairMat) {
    return this.itemRepairedBy.test(repaired, repairMat);
  }

  /**
   * Set the method determining whether an item is repaired by another item. This is not the same as
   * a repair via combination of like items! Like items always attempt to combine durability. If
   * you require different behavior, override {@link #apply(AnvilInventory)} and do not call
   * {@link AnvilFunction#REPAIR_WITH_COMBINATION}.
   *
   * <p>N.B. Only {@link org.bukkit.inventory.meta.Damageable Damageable} items can be repaired.
   * A material repair restores 25% of the durability of an item per material consumed.
   *
   * @param itemRepairedBy the method determining whether an item is repaired by another item
   */
  public void setItemRepairedBy(
      @NotNull BiPredicate<@NotNull ItemStack, @NotNull ItemStack> itemRepairedBy) {
    this.itemRepairedBy = itemRepairedBy;
  }

  /**
   * Get an {@link AnvilResult} for this anvil operation.
   *
   * @return the {@code AnvilResult}
   */
  public @NotNull AnvilResult apply(@NotNull AnvilInventory inventory) {
    AnvilOperationState state = new AnvilOperationState(this, inventory);

    if (ItemUtil.isEmpty(state.getBase().getItem())) {
      return AnvilResult.EMPTY;
    }

    // Apply base cost.
    state.apply(AnvilFunction.PRIOR_WORK_LEVEL_COST);

    if (ItemUtil.isEmpty(state.getAddition().getItem())) {
      if (state.apply(AnvilFunction.RENAME)) {
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

    state.apply(AnvilFunction.RENAME);
    // Apply prior work cost after rename.
    // Rename also applies a prior work cost but does not increase it.
    state.apply(AnvilFunction.UPDATE_PRIOR_WORK_COST);

    if (!state.apply(AnvilFunction.REPAIR_WITH_MATERIAL)) {
      // Only do combination repair if this is not a material repair.
      state.apply(AnvilFunction.REPAIR_WITH_COMBINATION);
    }

    // Differing from vanilla - since we use a custom determination for whether enchantments should
    // transfer (which defaults to indirectly mimicking vanilla), enchantments may need to be
    // applied from a material repair.
    state.apply(AnvilFunction.COMBINE_ENCHANTMENTS_JAVA_EDITION);

    return state.forge();
  }

}
