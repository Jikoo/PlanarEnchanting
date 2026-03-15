package com.github.jikoo.planarenchanting.anvil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

/**
 * Definitions of materials used in anvil repair operations.
 */
final class RepairMaterial { // TODO should this just be part of MetaVanillaBehavior?

  /**
   * Get whether an item is repairable by another item.
   *
   * <p>N.B. This is for pure material-based repair operations, not for combination operations.
   *
   * @param base the item to be repaired
   * @param addition the item used to repair
   * @return whether the addition material can repair the base material
   */
  static boolean repairs(@NotNull ItemStack base, @NotNull ItemStack addition) {
    Predicate<Material> predicate = MATERIALS_TO_REPAIRABLE.get(base.getType());
    return predicate != null && predicate.test(addition.getType());
  }

  private static final Map<Material, Predicate<Material>> MATERIALS_TO_REPAIRABLE = new HashMap<>();

  static {
    loadTags();
    loadLists();
  }

  private static void loadTags() {
    Map<NamespacedKey, Predicate<Material>> tags = new HashMap<>();
    for (Entry<@Nullable NamespacedKey, @Nullable NamespacedKey> entry : BakedRepairableData.getTags().entrySet()) {
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
    for (Entry<@Nullable NamespacedKey, List<@Nullable NamespacedKey>> entry : BakedRepairableData.getLists().entrySet()) {
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

  private RepairMaterial() {
    throw new IllegalStateException("Cannot instantiate static helper method container.");
  }

}
