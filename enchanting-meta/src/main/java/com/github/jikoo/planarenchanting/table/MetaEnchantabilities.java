package com.github.jikoo.planarenchanting.table;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
class MetaEnchantabilities implements EnchantabilityProvider {

  private final Map<NamespacedKey, Enchantability> byKey;

  MetaEnchantabilities() {
    byKey = new HashMap<>();
    for (Entry<Integer, Set<@Nullable NamespacedKey>> entry : BakedEnchantableData.get().entrySet()) {
      Enchantability enchantability = new Enchantability(entry.getKey());
      for (NamespacedKey key : entry.getValue()) {
        if (key == null) {
          continue;
        }
        Material material = Registry.MATERIAL.get(key);
        if (material != null && material.isItem()) {
          byKey.put(material.getKey(), enchantability);
        }
      }
    }
  }

  @Override
  public @Nullable Enchantability of(Material material) {
    return byKey.get(material.getKey());
  }

  @Override
  public @Nullable Enchantability of(ItemType itemType) {
    return byKey.get(itemType.getKey());
  }

  @Override
  public @Nullable Enchantability of(ItemStack item) {
    return byKey.get(item.getType().getKey());
  }

}
