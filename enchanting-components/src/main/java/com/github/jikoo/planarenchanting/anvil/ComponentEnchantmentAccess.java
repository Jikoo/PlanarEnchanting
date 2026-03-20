package com.github.jikoo.planarenchanting.anvil;

import com.github.jikoo.planarenchanting.util.EnchantmentAccess;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
class ComponentEnchantmentAccess implements EnchantmentAccess<ItemStack> {

  @Override
  public boolean isBook(ItemStack itemStack) {
    return itemStack.getType() == Material.ENCHANTED_BOOK;
  }

  @Override
  public Map<Enchantment, Integer> getEnchantments(ItemStack itemStack) {
    ItemEnchantments enchants;
    if (itemStack.getType() == Material.ENCHANTED_BOOK) {
      enchants = itemStack.getData(DataComponentTypes.STORED_ENCHANTMENTS);
    } else {
      enchants = itemStack.getData(DataComponentTypes.ENCHANTMENTS);
    }
    return enchants != null ? enchants.enchantments() : Map.of();
  }

  @Override
  public void addEnchantments(ItemStack itemStack, Map<Enchantment, Integer> enchantments) {
    ItemEnchantments enchants = ItemEnchantments.itemEnchantments(enchantments);
    if (itemStack.getType() == Material.ENCHANTED_BOOK) {
      itemStack.setData(DataComponentTypes.STORED_ENCHANTMENTS, enchants);
    } else {
      itemStack.setData(DataComponentTypes.ENCHANTMENTS, enchants);
    }
  }

}
