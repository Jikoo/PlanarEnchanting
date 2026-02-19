package com.github.jikoo.planarenchanting.table;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Enchantable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A representation of how easily an item can be enchanted.
 *
 * <p>Note that setting enchantability too high may actually cause some enchantments to be available
 * less frequently. For example, {@code minecraft:infinity} is available only when the effective
 * enchanting level after modifiers is between two hardcoded values. Higher enchantability
 * increases the calculated end number of a randomized bonus range starting at {@code 0}. The higher
 * the max, the higher the average. If the total number including the bonus exceeds the maximum, you
 * may see very rare enchantments generate at low levels or higher than intended enchantment rarity.
 */
@NullMarked
public record Enchantability(@Range(from = 1, to = Integer.MAX_VALUE) int value) {

  /**
   * Get the {@code Enchantability} of a {@link Material}. Will return {@code null} if not
   * enchantable in an enchanting table.
   *
   * @param material the {@code Material}
   * @return the {@code Enchantability} if enchantable
   */
  public static @Nullable Enchantability forMaterial(Material material) {
    if (!material.isItem()) {
      return null;
    }
    if (BY_KEY != null) {
      return BY_KEY.get(material.getKey());
    }
    ItemType type = material.asItemType();
    return type == null ? null : forType(type);
  }

  /**
   * Get the {@code Enchantability} of an {@link ItemType}. Will return {@code null} if not
   * enchantable in an enchanting table.
   *
   * @param type the {@code ItemType}
   * @return the {@code Enchantability} if enchantable
   */
  public static @Nullable Enchantability forType(ItemType type) {
    if (BY_KEY != null) {
      return BY_KEY.get(type.getKey());
    }

    Enchantable enchantable = type.getDefaultData(DataComponentTypes.ENCHANTABLE);
    return enchantable != null ? new Enchantability(enchantable.value()) : null;
  }

  /**
   * Get the {@code Enchantability} of an {@link ItemStack}. Will return {@code null} if not
   * enchantable in an enchanting table.
   *
   * @param item the {@code ItemStack}
   * @return the {@code Enchantability} if enchantable
   */
  public static @Nullable Enchantability forItem(ItemStack item) {
    if (BY_KEY != null) {
      return BY_KEY.get(item.getType().getKey());
    }

    Enchantable enchantable = item.getData(DataComponentTypes.ENCHANTABLE);
    return enchantable != null ? new Enchantability(enchantable.value()) : null;
  }

  private static final @Nullable Map<Key, Enchantability> BY_KEY;

  static {
    // Defend against potential refactors of Enchantable component.
    boolean enchantableAvailable;
    try {
      ItemType.DIAMOND_PICKAXE.getDefaultData(DataComponentTypes.ENCHANTABLE);
      enchantableAvailable = true;
    } catch (LinkageError ignored) {
      // Surprise, still compatible!
      enchantableAvailable = false;
    }

    if (enchantableAvailable) {
      // If enchants are available, set fallthrough to null.
      // This will serve as a flag in the forType implementation.
      BY_KEY = null;
    } else {
      // Otherwise, use values from last generated version.
      BY_KEY = new HashMap<>();
      for (Entry<Integer, Set<Key>> entry : BakedEnchantableData.get().entrySet()) {
        Enchantability enchantability = new Enchantability(entry.getKey());
        for (Key key : entry.getValue()) {
          ItemType type = Registry.ITEM.get(key);
          if (type != null) {
            BY_KEY.put(type.getKey(), enchantability);
          }
        }
      }
    }
  }

}
