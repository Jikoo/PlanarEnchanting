package com.github.jikoo.planarenchanting.table;

import com.github.jikoo.planarenchanting.util.ServerCapabilities;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jspecify.annotations.Nullable;

/**
 * A wrapper allowing access to the {@link Enchantability} of an item or item type.
 */
public class Enchantabilities {

  /**
   * Get the {@code Enchantability} of a {@link Material}. Will return {@code null} if not
   * enchantable in an enchanting table.
   *
   * @param material the {@code Material}
   * @return the {@code Enchantability} if enchantable
   */
  public static @Nullable Enchantability of(Material material) {
    return DELEGATE.of(material);
  }

  /**
   * Get the {@code Enchantability} of an {@link ItemType}. Will return {@code null} if not
   * enchantable in an enchanting table.
   *
   * @param type the {@code ItemType}
   * @return the {@code Enchantability} if enchantable
   */
  public static @Nullable Enchantability of(ItemType type) {
    return DELEGATE.of(type);
  }

  /**
   * Get the {@code Enchantability} of an {@link ItemStack}. Will return {@code null} if not
   * enchantable in an enchanting table.
   *
   * @param item the {@code ItemStack}
   * @return the {@code Enchantability} if enchantable
   */
  public static @Nullable Enchantability of(ItemStack item) {
    return DELEGATE.of(item);
  }

  private static final EnchantabilityProvider DELEGATE;

  static {
    if (ServerCapabilities.DATA_COMPONENT) {
      DELEGATE = new ComponentEnchantabilities();
    } else {
      DELEGATE = new MetaEnchantabilities();
    }
  }

  private Enchantabilities() {
    throw new IllegalStateException("Cannot instantiate static helper method container.");
  }

}
