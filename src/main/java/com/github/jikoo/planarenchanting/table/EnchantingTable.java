package com.github.jikoo.planarenchanting.table;

import com.github.jikoo.planarenchanting.enchant.EnchantData;
import com.github.jikoo.planarwrappers.util.WeightedRandom;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;
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
    Map<EnchantData, Integer> available = getAvailableResults(enchantQuality);

    Map<Enchantment, Integer> selected = new HashMap<>();
    // First enchantment added does not penalize enchantment quality.
    addEnchant(random, selected, available);

    while (!available.isEmpty() && random.nextInt(50) < enchantQuality) {
      addEnchant(random, selected, available);
      enchantQuality /= 2;
    }

    return selected;
  }

  private @NotNull Map<@NotNull EnchantData, @NotNull Integer> getAvailableResults(
      int enchantQuality) {
    Map<EnchantData, Integer> available = new HashMap<>();

    for (Enchantment enchantment : this.enchantments) {
      EnchantData data = EnchantData.of(enchantment);
      // Find a level appropriate for the finalized enchanting level.
      for (int lvl = maxLevel.applyAsInt(enchantment); lvl >= enchantment.getStartLevel(); --lvl) {
        if (enchantQuality >= data.getMinCost(lvl) && enchantQuality <= data.getMaxCost(lvl)) {
          available.put(data, lvl);
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
      @NotNull Map<EnchantData, Integer> available) {
    if (available.isEmpty())  {
      return;
    }

    // Select enchantment.
    EnchantData enchantData = WeightedRandom.choose(random, available.keySet());

    // Add selected enchantment and remove it from the available listings.
    selected.put(enchantData.getEnchantment(), available.remove(enchantData));

    // Remove all enchantment possibilities that conflict with the enchantment.
    available.keySet().removeIf(
        data -> this.incompatibility.test(data.getEnchantment(), enchantData.getEnchantment()));
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

    if (button == 0) {
      level = Math.max(level / 3, 1);
    } else if (button == 1) {
      level = level * 2 / 3 + 1;
    } else {
      level = Math.max(level, shelves * 2);
    }

    return level >= button + 1 ? level : 0;
  }

  /**
   * Update enchantment table buttons for a player on a tick delay. This fixes desync
   * problems that prevent the client from enchanting ordinarily un-enchantable objects.
   *
   * @param plugin the plugin sending the update
   * @param player the player enchanting
   * @param offers the enchantment offers
   */
  public static void updateButtons(@NotNull Plugin plugin, @NotNull Player player,
      @Nullable EnchantmentOffer @NotNull [] offers) {
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      for (int i = 1; i <= 3; ++i) {
        EnchantmentOffer offer = offers[i - 1];
        if (offer != null) {
          player.setWindowProperty(
              InventoryView.Property.valueOf("ENCHANT_BUTTON" + i),
              offer.getCost());
          player.setWindowProperty(
              InventoryView.Property.valueOf("ENCHANT_LEVEL" + i),
              offer.getEnchantmentLevel());
          player.setWindowProperty(
              InventoryView.Property.valueOf("ENCHANT_ID" + i),
              getEnchantmentId(offer.getEnchantment()));
        }
      }
    }, 1L);
  }

  /**
   * Get an {@link Enchantment Enchantment's} magic ID for use in packets.
   *
   * @param enchantment the {@code Enchantment}
   * @return the magic value or 0 if the value cannot be obtained
   */
  private static int getEnchantmentId(@NotNull Enchantment enchantment) {
    // Re-obtain from registry to ensure we have the internal enchantment.
    enchantment = Enchantment.getByKey(enchantment.getKey());
    if (enchantment == null) {
      // If the enchantment isn't registered, it won't have an ID anyway.
      return 0;
    }

    try {
      Class<?> clazzRegistry = Class.forName("net.minecraft.core.IRegistry");
      // NMSREF net.minecraft.core.Registry#ENCHANTMENT
      Object enchantmentRegistry = clazzRegistry.getDeclaredField("W").get(null);
      // NMSREF net.minecraft.core.Registry#getId(java.lang.Object)
      Method methodRegistryGetId = clazzRegistry.getDeclaredMethod("a", Object.class);

      Method getHandle = enchantment.getClass().getDeclaredMethod("getHandle");

      return (int) methodRegistryGetId.invoke(enchantmentRegistry, getHandle.invoke(enchantment));
    } catch (ReflectiveOperationException | ClassCastException e) {
      // Fall through to using declaration order.
      // Bukkit does match Minecraft's declaration order, but it's not safe to rely on.
      Enchantment[] enchantments = Enchantment.values();
      for (int i = 0; i < enchantments.length; i++) {
        if (enchantments[i].getKey().equals(enchantment.getKey())) {
          return i;
        }
      }
    }

    // Default for unknown values.
    return 0;
  }

}
