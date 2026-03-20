package com.github.jikoo.planarenchanting.util;

import com.github.jikoo.planarenchanting.util.EnchantData.Provider;
import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.enchantments.Enchantment;
import org.jspecify.annotations.NullMarked;

@NullMarked
class ComponentEnchantProvider implements Provider {

  @Override
  public EnchantData of(Enchantment enchantment) {
    return new EnchantData() {
      @Override
      public int getWeight() {
        return enchantment.getWeight();
      }

      @Override
      public int getAnvilCost() {
        return enchantment.getAnvilCost();
      }

      @Override
      public int getMinModifiedCost(int level) {
        return enchantment.getMinModifiedCost(level);
      }

      @Override
      public int getMaxModifiedCost(int level) {
        return enchantment.getMaxModifiedCost(level);
      }

      @Override
      public boolean isTridentEnchant() {
        return enchantment.getSupportedItems().contains(ItemTypeKeys.TRIDENT);
      }
    };
  }

}
