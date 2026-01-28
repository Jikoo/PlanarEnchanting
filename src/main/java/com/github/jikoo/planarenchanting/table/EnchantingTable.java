package com.github.jikoo.planarenchanting.table;

import com.github.jikoo.planarwrappers.util.WeightedRandom;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A container for data required to calculate enchantments.
 */
public class EnchantingTable {

  private final @NotNull Collection<@NotNull Enchantment> enchantments;
  private final @NotNull Enchantability enchantability;
  private @NotNull BiPredicate<@NotNull Enchantment, @NotNull Enchantment> incompatibility;
  private @NotNull ToIntFunction<@NotNull Enchantment> maxLevel;

  /**
   * Construct a new {@code EnchantingTable}.
   *
   * @param enchantments the enchantments that may be applied
   * @param enchantability the {@link Enchantability} of the object to be enchanted
   */
  public EnchantingTable(
      @NotNull Collection<@NotNull Enchantment> enchantments,
      @NotNull Enchantability enchantability) {
    this.enchantments = enchantments;
    this.enchantability = enchantability;
    this.incompatibility = (ench1, ench2) -> ench1.equals(ench2) || ench1.conflictsWith(ench2);
    this.maxLevel = Enchantment::getMaxLevel;
  }

  /**
   * Set the method determining if two {@link Enchantment Enchantments} are incompatible.
   *
   * <p>Note that identical enchantments are always incompatible - the same enchantment cannot be
   * applied twice. This is handled internally and does not need to be included in the comparison.
   *
   * @param incompatibility the incompatibility comparison
   */
  public void setIncompatibility(
      @NotNull BiPredicate<@NotNull Enchantment, @NotNull Enchantment> incompatibility) {
    this.incompatibility = (e1, e2) -> e1.equals(e2) || incompatibility.test(e1, e2);
  }

  /**
   * Set the method determining the maximum level of an enchantment.
   *
   * <p>Please do not blindly make large blanket changes (i.e. {@code enchantment -> (int)
   * Short.MAX_VALUE}). Depending on the enchantment, max level changes may be useless or actively
   * detrimental. For example, {@code minecraft:infinity} uses a hardcoded minimum and maximum
   * effective enchanting level, ignoring level differences. If higher levels are available, those
   * will always be generated if the enchantment is selected and eligible.
   *
   * @param maxLevel the max level function
   */
  public void setMaxLevel(@NotNull ToIntFunction<@NotNull Enchantment> maxLevel) {
    this.maxLevel = maxLevel;
  }

  /**
   * Get the {@link Enchantment Enchantments} resulting from the enchanting operation.
   *
   * @param random the {@link Random} instance used for number generation
   * @param enchantLevel the level of the enchantment
   * @return the results of the enchanting operation
   */
  public @NotNull Map<Enchantment, Integer> apply(@NotNull Random random, int enchantLevel) {

    // Ensure enchantments present.
    if (this.enchantments.isEmpty() || enchantLevel < 1) {
      return Collections.emptyMap();
    }

    // Determine effective level.
    int enchantQuality = getEnchantQuality(random, enchantLevel);

    // Remap to EnchantData, collecting in a HashSet for later modifications.
    Map<Enchantment, Integer> available = getAvailableResults(enchantQuality);

    Map<Enchantment, Integer> selected = new HashMap<>();
    // First enchantment added does not penalize enchantment quality.
    addEnchant(random, selected, available);

    while (!available.isEmpty() && random.nextInt(50) < enchantQuality) {
      addEnchant(random, selected, available);
      enchantQuality /= 2;
    }

    return selected;
  }

  private @NotNull Map<@NotNull Enchantment, @NotNull Integer> getAvailableResults(
      int enchantQuality) {
    Map<Enchantment, Integer> available = new HashMap<>();

    for (Enchantment enchantment : this.enchantments) {
      // Find a level appropriate for the finalized enchanting level.
      for (int lvl = maxLevel.applyAsInt(enchantment); lvl >= enchantment.getStartLevel(); --lvl) {
        if (enchantQuality >= enchantment.getMinModifiedCost(lvl)
            && enchantQuality <= enchantment.getMaxModifiedCost(lvl)) {
          available.put(enchantment, lvl);
          break;
        }
      }
    }

    return available;
  }

  /**
   * Randomly select and add an {@link Enchantment}.
   *
   * @param random the {@link Random} instance used for number generation
   * @param selected the map of already-selected {@code Enchantments}
   * @param available the available {@code Enchantments}
   */
  private void addEnchant(
      @NotNull Random random,
      @NotNull Map<Enchantment, Integer> selected,
      @NotNull Map<Enchantment, Integer> available) {
    if (available.isEmpty())  {
      return;
    }

    // Select enchantment.
    Enchantment choice = WeightedRandom.choose(random, available.keySet(), Enchantment::getWeight);

    // Add selected enchantment and remove it from the available listings.
    selected.put(choice, available.remove(choice));

    // Remove all enchantment possibilities that conflict with the enchantment.
    available.keySet().removeIf(
        data -> this.incompatibility.test(data, choice));
  }

  /**
   * Determine enchantment quality for the given enchantability and enchantment level. In vanilla,
   * level is modified to generate tier of enchantment. As the idea of selected level and generation
   * level gets messy, this is referred to as "quality" instead.
   *
   * @param random the {@link Random} instance used for number generation
   * @param enchantLevel the level of the enchantment
   * @return the enchantment quality
   */
  private int getEnchantQuality(@NotNull Random random, final int enchantLevel) {
    /*
     * Rather than run RNG twice, run once with a max bound 1 lower.
     * Vanilla:
     * enchantLevel += 1
     *   + random.nextInt(enchantability / 4 + 1)
     *   + random.nextInt(enchantability / 4 + 1)
     */
    int enchantQuality = 2 * (enchantability.value() / 4 + 1) - 1;
    enchantQuality = 1 + random.nextInt(enchantQuality);
    enchantQuality += enchantLevel;

    /*
     * Add random enchantability penalty/bonus of 85-115%
     * Vanilla:
     * float bonus = (random.nextFloat() + random.nextFloat() - 1.0F) * 0.15F
     */
    float bonus = (random.nextFloat(2) - 1F) * 0.15F;
    enchantQuality = Math.round(enchantQuality + enchantQuality * bonus);

    return Math.max(1, enchantQuality);
  }

  /**
   * Get an {@link EnchantmentOffer} of the first enchantment rolled for the given.
   *
   * @param random the {@link Random} instance used for number generation
   * @param enchantLevel the level of the enchantment
   * @return the offer or null if no enchantments will be available
   */
  public @Nullable EnchantmentOffer getOffer(
      @NotNull Random random,
      int enchantLevel) {
    // If level is too low, no offer.
    if (enchantLevel < 1) {
      return null;
    }

    // Calculate enchantments offered for levels offered.
    var rolledEnchants = this.apply(random, enchantLevel);

    // Get offer for first enchantment if present, otherwise return null.
    return rolledEnchants.entrySet().stream().findFirst()
        .map(entry -> new EnchantmentOffer(entry.getKey(), entry.getValue(), enchantLevel))
        .orElse(null);
  }

  /**
   * Get three integers representing button levels in an enchanting table.
   *
   * @param random the {@link Random} to be used for generation
   * @param shelves the number of bookshelves to use when calculating levels
   * @return an array of three integers
   */
  public static int @NotNull [] getButtonLevels(@NotNull Random random, int shelves) {
    shelves = Math.min(shelves, 15);
    int[] levels = new int[3];

    for (int button = 0; button < 3; ++button) {
      levels[button] = getButtonLevel(random, button, shelves);
    }

    return levels;
  }

  /**
   * Get an integer for a button's level requirement in an enchanting table.
   *
   * @param random the {@link Random} to be used for generation
   * @param button the number of the button
   * @param shelves the number of bookshelves present
   * @return the calculated button level
   */
  private static int getButtonLevel(@NotNull Random random, int button, int shelves) {
    int level = random.nextInt(8) + 1 + (shelves >> 1) + random.nextInt(shelves + 1);

    level = switch (button) {
      case 0 -> Math.max(level / 3, 1);
      case 1 -> level * 2 / 3 + 1;
      default -> Math.max(level, shelves * 2);
    };

    return level >= button + 1 ? level : 0;
  }

}
