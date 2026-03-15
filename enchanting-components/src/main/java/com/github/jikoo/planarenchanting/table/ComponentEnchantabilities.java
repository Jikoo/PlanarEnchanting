package com.github.jikoo.planarenchanting.table;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Enchantable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
class ComponentEnchantabilities implements EnchantabilityProvider {

  @Override
  public @Nullable Enchantability of(Material material) {
    ItemType itemType = material.asItemType();
    return itemType != null ? of(itemType) : null;
  }

  @Override
  public @Nullable Enchantability of(ItemType itemType) {
    Enchantable enchantable = itemType.getDefaultData(DataComponentTypes.ENCHANTABLE);
    return enchantable != null ? new Enchantability(enchantable.value()) : null;
  }

  @Override
  public @Nullable Enchantability of(ItemStack item) {
    Enchantable enchantable = item.getData(DataComponentTypes.ENCHANTABLE);
    return enchantable != null ? new Enchantability(enchantable.value()) : null;
  }
}
