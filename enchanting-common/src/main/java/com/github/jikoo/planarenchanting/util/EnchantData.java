package com.github.jikoo.planarenchanting.util;

import java.util.ServiceLoader;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/**
 * Enchantment details that are either not available via the standard Bukkit enchantment
 * API or which are fetched differently using Paper's API.
 */
@NullMarked
public interface EnchantData {

  /**
   * Get the weight of the enchantment. Higher weights are more likely to be produced by an
   * enchanting table.
   *
   * @return the enchantment weight
   */
  int getWeight();

  /**
   * Get the cost modifier of applying the enchantment in an anvil.
   *
   * @return the anvil cost modifier
   */
  int getAnvilCost();

  /**
   * Get the minimum enchanting table roll cost of an enchantment at a certain level.
   *
   * @param level the level of the enchantment
   * @return the minimum roll required for the enchantment at the specified level
   */
  int getMinModifiedCost(int level);

  /**
   * Get the maximum enchanting table roll cost of an enchantment at a certain level.
   *
   * @param level the level of the enchantment
   * @return the maximum roll required for the enchantment at the specified level
   */
  int getMaxModifiedCost(int level);

  /**
   * Check if an enchantment is a trident enchantment.
   *
   * @return true if the enchantment is a trident-exclusive enchantment
   */
  boolean isTridentEnchant();

  /**
   * An enchantment data provider.
   *
   * @see Service#PROVIDER
   */
  @NullMarked
  interface Provider {

    EnchantData of(Enchantment enchantment);

  }

  @ApiStatus.NonExtendable
  interface Service {

    /**
     * A {@link Provider} loaded from a service.
     */
    Provider PROVIDER = ServiceLoader.load(Provider.class, Provider.class.getClassLoader())
        .findFirst().orElseThrow();

  }

}
