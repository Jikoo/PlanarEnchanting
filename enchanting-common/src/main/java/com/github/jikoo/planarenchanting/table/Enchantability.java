package com.github.jikoo.planarenchanting.table;

import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NullMarked;

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
public record Enchantability(@Range(from = 1, to = Integer.MAX_VALUE) int value) {}
