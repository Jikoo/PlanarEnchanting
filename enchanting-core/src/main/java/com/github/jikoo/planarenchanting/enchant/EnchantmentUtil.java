package com.github.jikoo.planarenchanting.enchant;

import java.util.Map;
import java.util.function.BiConsumer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Common enchantment-related functions.
 */
public final class EnchantmentUtil {

  /**
   * Get {@link Enchantment Enchantments} from an {@link ItemMeta}.
   *
   * @param meta the {@code ItemMeta}
   * @return the stored enchantments
   */
  public static @NotNull Map<Enchantment, Integer> getEnchants(@Nullable ItemMeta meta) {
    if (meta == null) {
      return Map.of();
    }

    if (meta instanceof EnchantmentStorageMeta enchantmentStorageMeta) {
      return enchantmentStorageMeta.getStoredEnchants();
    }

    return meta.getEnchants();
  }

  /**
   * Add {@link Enchantment Enchantments} to an {@link ItemMeta}.
   *
   * <p>N.B. This is an add operation, not set! Existing enchantments not specified are not removed.
   *
   * @param meta the {@code ItemMeta}
   * @param enchants the enchantments to add
   */
  public static void addEnchants(
      @Nullable ItemMeta meta,
      @NotNull Map<Enchantment, Integer> enchants) {
    if (meta == null) {
      return;
    }

    BiConsumer<Enchantment, Integer> addEnchant;
    if (meta instanceof EnchantmentStorageMeta storageMeta) {
      addEnchant = (enchant, level) -> storageMeta.addStoredEnchant(enchant, level, true);
    } else {
      addEnchant = (enchant, level) -> meta.addEnchant(enchant, level, true);
    }
    enchants.forEach(addEnchant);
  }

  private EnchantmentUtil() {
    throw new IllegalStateException("Cannot instantiate static helper method container.");
  }

}
