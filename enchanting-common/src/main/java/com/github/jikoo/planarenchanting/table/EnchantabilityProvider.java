package com.github.jikoo.planarenchanting.table;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
interface EnchantabilityProvider {

  @Nullable Enchantability of(Material material);

  @Nullable Enchantability of(ItemType itemType);

  @Nullable Enchantability of(ItemStack item);

}
