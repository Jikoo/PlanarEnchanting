package com.github.jikoo.planarenchanting.anvil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemType;
import org.jspecify.annotations.NullMarked;

/**
 * {@link org.bukkit.inventory.meta.ItemMeta}-based {@link AnvilBehavior}.
 * Uses pre-baked data to cover missing API.
 */
@NullMarked
public class MetaVanillaBehavior implements AnvilBehavior<MetaCachedStack> {

  @Override
  public boolean enchantApplies(Enchantment enchantment, MetaCachedStack base) {
    return enchantment.canEnchantItem(base.getItem());
  }

  @Override
  public boolean itemsCombineEnchants(MetaCachedStack base, MetaCachedStack addition) {
    Material addedType = addition.getItem().getType();
    return base.getItem().getType() == addedType || addedType == Material.ENCHANTED_BOOK;
  }

  @Override
  public boolean itemRepairedBy(MetaCachedStack repaired, MetaCachedStack repairMat) {
    Predicate<Material> predicate = MATERIALS_TO_REPAIRABLE.get(repaired.getItem().getType());
    return predicate != null && predicate.test(repairMat.getItem().getType());
  }

  private static final Map<Material, Predicate<Material>> MATERIALS_TO_REPAIRABLE = new HashMap<>();

  static {
    loadTags();
    loadLists();
  }

  private static void loadTags() {
    Map<NamespacedKey, Predicate<Material>> tags = new HashMap<>();
    for (var entry : BakedRepairableData.getTags().entrySet()) {
      if (entry.getKey() == null || entry.getValue() == null) {
        continue;
      }

      Material mat = Registry.MATERIAL.get(entry.getKey());
      if (mat == null) {
        continue;
      }

      Predicate<Material> predicate = tags.computeIfAbsent(
          entry.getValue(),
          tagKey -> {
            // Prefer Material tags as they're officially supported.
            Tag<Material> matTag = Bukkit.getTag(Tag.REGISTRY_ITEMS, entry.getValue(), Material.class);
            if (matTag != null) {
              return matTag::isTagged;
            }

            // Fall through to ItemType tags.
            Tag<ItemType> typeTag = Bukkit.getTag(Tag.REGISTRY_ITEMS, entry.getValue(), ItemType.class);
            if (typeTag == null) {
              return null;
            }
            return localMat -> {
              ItemType localType = localMat.asItemType();
              return localType != null && typeTag.isTagged(localType);
            };

          }
      );

      if (predicate != null) {
        MATERIALS_TO_REPAIRABLE.put(mat, predicate);
      }
    }
  }

  private static void loadLists() {
    for (var entry : BakedRepairableData.getLists().entrySet()) {
      if (entry.getKey() == null) {
        continue;
      }

      Material type = Registry.MATERIAL.get(entry.getKey());
      if (type == null || !type.isItem()) {
        continue;
      }

      Set<Material> values = new HashSet<>();
      for (NamespacedKey key : entry.getValue()) {
        if (key == null) {
          continue;
        }
        Material value = Registry.MATERIAL.get(key);
        if (value != null) {
          values.add(value);
        }
      }

      if (!values.isEmpty()) {
        MATERIALS_TO_REPAIRABLE.put(type, values::contains);
      }
    }
  }

}
