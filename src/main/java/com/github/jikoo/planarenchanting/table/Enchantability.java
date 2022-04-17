package com.github.jikoo.planarenchanting.table;

import org.jetbrains.annotations.Range;

/**
 * Representation of materials' enchantability.
 *
 * <p>Note that setting enchantability too high may actually cause some enchantments to be available
 * less frequently. For example, {@code minecraft:infinity} is available only when the effective
 * enchanting level after modifiers is between two hardcoded values. Higher enchantability
 * increases the calculated end number of a randomized bonus range starting at {@code 0}. The higher
 * the max, the higher the average. If the total number including the bonus exceeds the maximum, you
 * may see very rare enchantments generate at low levels or higher than intended enchantment rarity.
 */
public record Enchantability(@Range(from = 1, to = Integer.MAX_VALUE) int value) {

  // TODO index to convert from Material?

  public static final Enchantability LEATHER = new Enchantability(15);
  public static final Enchantability CHAIN = new Enchantability(12);
  public static final Enchantability IRON_ARMOR = new Enchantability(9);
  public static final Enchantability GOLD_ARMOR = new Enchantability(25);
  public static final Enchantability DIAMOND = new Enchantability(10);
  public static final Enchantability TURTLE = IRON_ARMOR;
  public static final Enchantability NETHERITE = LEATHER;
  public static final Enchantability WOOD = LEATHER;
  public static final Enchantability STONE = new Enchantability(5);
  public static final Enchantability IRON_TOOL = new Enchantability(14);
  public static final Enchantability GOLD_TOOL = new Enchantability(22);
  public static final Enchantability BOOK = new Enchantability(1);
  public static final Enchantability TRIDENT = BOOK;

}
