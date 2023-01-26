package com.github.jikoo.planarenchanting.util;

import be.seeseemelk.mockbukkit.enchantments.EnchantmentMock;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Helper for overcoming some shortcomings of MockBukkit.
 */
public final class EnchantmentHelper {

  private static final Map<NamespacedKey, Enchantment> KEYS_TO_ENCHANTS;

  static {
    try {
      Field byKey = Enchantment.class.getDeclaredField("byKey");
      byKey.setAccessible(true);
      //noinspection unchecked
      KEYS_TO_ENCHANTS = (Map<NamespacedKey, Enchantment>) byKey.get(null);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

  public static @NotNull Collection<Enchantment> getRegisteredEnchantments() {
    return KEYS_TO_ENCHANTS.values();
  }

  public static void putEnchant(Enchantment enchantment) {
    KEYS_TO_ENCHANTS.put(enchantment.getKey(), enchantment);
  }

  private static void wrapCanEnchant() {
    getRegisteredEnchantments().stream().map(enchantment -> {
      EnchantmentMock mock = new EnchantmentMock(enchantment.getKey(),
          enchantment.getKey().getKey()) {
        @Override
        public boolean canEnchantItem(@NotNull ItemStack item) {
          // MockBukkit doesn't set up enchantment targets
          return getItemTarget() != null && getItemTarget().includes(item);
        }

        @Override
        public boolean conflictsWith(@NotNull Enchantment other) {
          // Don't overwrite conflict determination method.
          return enchantment.conflictsWith(other);
        }
      };
      EnchantmentTarget itemTarget = enchantment.getItemTarget();
      if (itemTarget != null) {
        mock.setItemTarget(itemTarget);
      }
      mock.setMaxLevel(enchantment.getMaxLevel());
      mock.setStartLevel(1);
      // Up to MockBukkit to remove support for curses
      mock.setCursed(enchantment.isCursed());
      mock.setTreasure(enchantment.isTreasure());

      return mock;
    }).forEach(EnchantmentHelper::putEnchant);
  }

  public static void setupToolEnchants() {
    // Only replace conflict determination if the enchantment is a basic MockBukkit enchantment.
    if (Enchantment.SILK_TOUCH.getClass() == EnchantmentMock.class) {
      putEnchant(
          new EnchantmentMock(
              Enchantment.SILK_TOUCH.getKey(), Enchantment.SILK_TOUCH.getKey().getKey()) {
            @Override
            public boolean conflictsWith(@NotNull Enchantment other) {
              return other.equals(this) || other.equals(Enchantment.LOOT_BONUS_BLOCKS);
            }
          });
    }
    if (Enchantment.LOOT_BONUS_BLOCKS.getClass() == EnchantmentMock.class) {
      putEnchant(
          new EnchantmentMock(
              Enchantment.LOOT_BONUS_BLOCKS.getKey(),
              Enchantment.LOOT_BONUS_BLOCKS.getKey().getKey()) {
            @Override
            public boolean conflictsWith(@NotNull Enchantment other) {
              return other.equals(this) || other.equals(Enchantment.SILK_TOUCH);
            }
          });
    }

    // Wrap canEnchant to use EnchantmentTarget.
    wrapCanEnchant();

    // Set up details for enchantments that can target tools.
    setupEnchant("efficiency", 5, EnchantmentTarget.TOOL);
    setupEnchant("unbreaking", 3, EnchantmentTarget.BREAKABLE);
    setupEnchant("fortune", 3, EnchantmentTarget.TOOL);
    setupEnchant("silk_touch", 1, EnchantmentTarget.TOOL);
    setupEnchant("mending", 1, EnchantmentTarget.BREAKABLE);
  }

  public static void setupEnchant(String id, int levelMax, EnchantmentTarget target) {
    EnchantmentMock mock = (EnchantmentMock) Enchantment.getByKey(NamespacedKey.minecraft(id));
    assert mock != null;
    mock.setMaxLevel(levelMax);
    mock.setStartLevel(1);
    mock.setItemTarget(target);
  }

  private EnchantmentHelper() {
    throw new IllegalStateException("Cannot instantiate static helper method container.");
  }

}
