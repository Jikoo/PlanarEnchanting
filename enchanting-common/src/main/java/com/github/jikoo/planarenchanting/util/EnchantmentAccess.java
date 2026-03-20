package com.github.jikoo.planarenchanting.util;

import java.util.Map;
import org.bukkit.enchantments.Enchantment;
import org.jspecify.annotations.NullMarked;

/**
 * Defines behavior for accessing enchantment details from an item implementation.
 *
 * @param <T> the type of item
 */
@NullMarked
public interface EnchantmentAccess<T> {

  /**
   * Gets whether an item represents an enchanted book.
   *
   * @param t the item
   * @return true if the item represents an enchanted book
   */
  boolean isBook(T t);

  /**
   * Gets the enchantments from an item. For normal items, this retrieves active enchantments.
   * For enchanted books, this retrieves stored enchantments.
   *
   * @param t the item
   * @return the enchantments on the item
   */
  Map<Enchantment, Integer> getEnchantments(T t);

  /**
   * Adds enchantments to an item. For normal items, this applies active enchantments. For
   * enchanted books, this applies stored enchantments.
   *
   * <p>This does not remove enchantments that are not modified!</p>
   *
   * @param t the item
   * @param enchantments the set of enchantments to add
   */
  void addEnchantments(T t, Map<Enchantment, Integer> enchantments);

}
