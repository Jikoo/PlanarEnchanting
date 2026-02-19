package com.github.jikoo.planarenchanting.anvil;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Repairable;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

/**
 * Definitions of materials used in anvil repair operations.
 */
public final class RepairMaterial {

  /**
   * Get whether an item is repairable by another item.
   *
   * <p>N.B. This is for pure material-based repair operations, not for combination operations.
   *
   * @param base the item to be repaired
   * @param addition the item used to repair
   * @return whether the addition material can repair the base material
   */
  public static boolean repairs(@NotNull ItemStack base, @NotNull ItemStack addition) {
    if (MATERIALS_TO_REPAIRABLE != null) {
      Predicate<Key> predicate = MATERIALS_TO_REPAIRABLE.get(base.getType().getKey());
      return predicate != null && predicate.test(addition.getType().getKey());
    }

    Repairable repairable = base.getData(DataComponentTypes.REPAIRABLE);

    if (repairable == null) {
      return false;
    }

    // Note: KeyImpl and NamespacedKey have the same hashCode implementation, so
    // the fact that ItemTypeKeys all have a KeyImpl instead shouldn't matter.
    TypedKey<ItemType> itemKey = TypedKey.create(RegistryKey.ITEM, addition.getType().getKey());
    return repairable.types().contains(itemKey);
  }

  private static final Map<Key, Predicate<Key>> MATERIALS_TO_REPAIRABLE;

  static {
    // Defend against possible refactors of Repairable component.
    boolean repairableAvailable;
    try {
      ItemType.DIAMOND_PICKAXE.getDefaultData(DataComponentTypes.REPAIRABLE);
      repairableAvailable = true;
    } catch (LinkageError ignored) {
      repairableAvailable = false;
    }

    if (repairableAvailable) {
      MATERIALS_TO_REPAIRABLE = null;
    } else {
      MATERIALS_TO_REPAIRABLE = new HashMap<>();
      loadTags();
      loadLists();
    }
  }

  private static void loadTags() {
    Map<TagKey<ItemType>, Predicate<Key>> tags = new HashMap<>();
    for (Entry<Key, Key> entry : BakedRepairableData.getTags().entrySet()) {
      ItemType type = Registry.ITEM.get(entry.getKey());
      if (type == null) {
        continue;
      }

      TagKey<ItemType> tagKey = TagKey.create(RegistryKey.ITEM, entry.getValue());
      if (!Registry.ITEM.hasTag(tagKey)) {
        continue;
      }

      RepairMaterial.MATERIALS_TO_REPAIRABLE.put(
          type.getKey(),
          tags.computeIfAbsent(tagKey, innerTagKey -> {
            Tag<ItemType> tag = Registry.ITEM.getTag(innerTagKey);
            return key -> tag.contains(TypedKey.create(RegistryKey.ITEM, key));
          })
      );
    }
  }

  private static void loadLists() {
    for (Entry<Key, List<Key>> entry : BakedRepairableData.getLists().entrySet()) {
      ItemType type = Registry.ITEM.get(entry.getKey());
      if (type == null) {
        continue;
      }

      // Note: A Key may be either a KeyImpl or NamespacedKey instance.
      // This is a Set rather than a List to make use the fact that they have identical (but not
      // shared, oh boy fragility!) hashCode implementations to check containment without iterating
      // over every element and manually checking namespace and key.
      Set<Key> values = new HashSet<>();
      for (Key key : entry.getValue()) {
        ItemType value = Registry.ITEM.get(key);
        if (value != null) {
          values.add(value.getKey());
        }
      }

      if (!values.isEmpty()) {
        MATERIALS_TO_REPAIRABLE.put(type.getKey(), values::contains);
      }
    }
  }

  private RepairMaterial() {
    throw new IllegalStateException("Cannot instantiate static helper method container.");
  }

}
