package com.github.jikoo.planarenchanting.anvil;

import com.github.jikoo.planarenchanting.util.ItemUtil;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A container for the result of an anvil operation.
 *
 * @see org.bukkit.inventory.AnvilInventory#setItem(int, ItemStack)
 * @see org.bukkit.inventory.AnvilInventory#setRepairCost(int)
 * @see org.bukkit.inventory.AnvilInventory#setRepairCostAmount(int)
 * @param item the result slot {@link ItemStack}
 * @param levelCost the number of levels to be consumed by the operation
 * @param materialCost the amount of items to be consumed from the addition slot
 */
public record AnvilResult(@NotNull ItemStack item, int levelCost, int materialCost) {

  public static final AnvilResult EMPTY = new AnvilResult(ItemUtil.AIR, 0, 0);

}
