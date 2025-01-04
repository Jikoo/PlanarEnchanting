package com.github.jikoo.planarenchanting.enchant;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntUnaryOperator;
import java.util.logging.Logger;
import com.github.jikoo.planarenchanting.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A utility for using the Paper API to access {@link EnchantData} details.
 * TODO may want to split this to a separate module for simplicity
 */
final class EnchantDataPaper {

  private static final MethodHandle GET_WEIGHT;
  private static final MethodHandle GET_ANVIL_COST;
  private static final MethodHandle GET_MIN_COST;
  private static final MethodHandle GET_MAX_COST;
  private static final MethodHandle GET_PRIMARY_ITEMS;
  private static final MethodHandle GET_SECONDARY_ITEMS;
  private static final MethodHandle REGISTRY_KEY_SET_CONTAINS;
  private static final Tag<Enchantment> TREASURE;

  static {
    MethodHandles.Lookup lookup = MethodHandles.publicLookup();

    MethodType intType = MethodType.methodType(int.class);
    GET_WEIGHT = getEnchantMethod(lookup, "getWeight", intType);
    GET_ANVIL_COST = getEnchantMethod(lookup, "getAnvilCost", intType);

    MethodType intUrnaryType = MethodType.methodType(int.class, int.class);
    GET_MIN_COST = getEnchantMethod(lookup, "getMinModifiedCost", intUrnaryType);
    GET_MAX_COST = getEnchantMethod(lookup, "getMaxModifiedCost", intUrnaryType);

    MethodHandle primary;
    MethodHandle secondary;
    MethodHandle contains;
    try {
      Class<?> registryClazz = Class.forName("io.papermc.paper.registry.set.RegistryKeySet");
      MethodType registryType = MethodType.methodType(registryClazz);
      primary = getEnchantMethod(lookup, "getPrimaryItems", registryType);
      secondary = getEnchantMethod(lookup, "getSecondaryItems", registryType);

      try {
        MethodType itemTypePredicateType = MethodType.methodType(boolean.class, ItemType.class);
        contains = lookup.findVirtual(registryClazz, "contains", itemTypePredicateType);
      } catch (NoSuchMethodException | IllegalAccessException e) {
        contains = null;
      }
    } catch (ClassNotFoundException e) {
      warnSpigot();
      primary = null;
      secondary = null;
      contains = null;
    }
    GET_PRIMARY_ITEMS = primary;
    GET_SECONDARY_ITEMS = secondary;
    REGISTRY_KEY_SET_CONTAINS = contains;

    Tag<Enchantment> treasureTag;
    try {
      Class<?> enchantTagKeysClazz = Class.forName("io.papermc.paper.registry.keys.tags.EnchantmentTagKeys");
      Class<?> tagKeyClazz = Class.forName("io.papermc.paper.registry.tag.TagKey");
      MethodHandle treasureRegistry = lookup.findStaticGetter(enchantTagKeysClazz, "TREASURE", tagKeyClazz);
      Object treasureKey = treasureRegistry.invoke();
      MethodType tagTagKeyType = MethodType.methodType(Tag.class, tagKeyClazz);
      MethodHandle tagLookup = lookup.findStatic(Registry.class, "getTag", tagTagKeyType);
      // Unchecked cast is okay; worst case scenario Paper changes the type in it and causes our
      // containment checks to always fail.
      //noinspection unchecked
      treasureTag = (Tag<Enchantment>) tagLookup.invoke(treasureKey);
    } catch (Throwable e) {
      warnSpigot();
      treasureTag = null;
    }
    TREASURE = treasureTag;
  }

  private static @Nullable MethodHandle getEnchantMethod(
      @NotNull MethodHandles.Lookup lookup,
      @NotNull String name,
      @NotNull MethodType type) {
    try {
      return lookup.findVirtual(Enchantment.class, name, type);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      warnSpigot();
      return null;
    }
  }

  private static boolean warnSpigot = !Boolean.getBoolean("planarIgnoreSpigot7838");
  private static void warnSpigot() {
    if (!warnSpigot) {
      return;
    }

    warnSpigot = false;

    Logger logger;
    try {
      JavaPlugin plugin = JavaPlugin.getProvidingPlugin(EnchantDataPaper.class);
      logger = plugin.getLogger();
    } catch (IllegalArgumentException e) {
      logger = Bukkit.getLogger();
    }

    logger.warning(() -> "[PlanarEnchanting] Unsupported enchantments detected! Spigot is missing API for enchantment data.");
    logger.warning(() -> "[PlanarEnchanting] Please vote for https://hub.spigotmc.org/jira/browse/SPIGOT-7838");
    logger.warning(() -> "[PlanarEnchanting] In the mean time, consider swapping to Paper or a derivative.");
  }

  /**
   * Fetch an {@link Enchantment Enchantment's} internal weight.
   *
   * @param enchantment the {@code Enchantment}
   * @return the weight or 0 if unknown
   */
  static int getWeight(@NotNull Enchantment enchantment) {
    if (GET_WEIGHT != null) {
      try {
        return (int) GET_WEIGHT.invoke(enchantment);
      } catch (Throwable ignored) {
        // This shouldn't be possible unless the implementation is modified to throw a runtime exception here.
      }
    }
    return 0;
  }

  /**
   * Fetch an {@link Enchantment Enchantment's} internal anvil cost multiplier.
   *
   * @param enchantment the {@code Enchantment}
   * @return the anvil cost multiplier or 40 if unknown
   */
  static int getAnvilCost(@NotNull Enchantment enchantment) {
    if (GET_ANVIL_COST != null) {
      try {
        return (int) GET_ANVIL_COST.invoke(enchantment);
      } catch (Throwable ignored) {
        // This shouldn't be possible unless the implementation is modified to throw a runtime exception here.
      }
    }
    return 40;
  }

  /**
   * Fetch an {@link Enchantment Enchantment's} internal minimum quality calculation method.
   *
   * @param enchantment the {@code Enchantment}
   * @return the internal method or the default value if the internal method is not available
   */
  static IntUnaryOperator getMinCost(Enchantment enchantment) {
    return level -> {
      if (GET_MIN_COST != null) {
        try {
          return (int) GET_MIN_COST.invoke(enchantment, level);
        } catch (Throwable ignored) {
          // This shouldn't be possible unless the implementation is modified to throw a runtime exception here.
        }
      }
      // Default to common rarity equivalent.
      return 1 + level * 10;
    };
  }

  /**
   * Fetch an {@link Enchantment Enchantment's} internal maximum quality calculation method.
   *
   * @param enchantment the {@code Enchantment}
   * @return the internal method or the default value if the internal method is not available
   */
  static IntUnaryOperator getMaxCost(Enchantment enchantment) {
    return level -> {
      if (GET_MAX_COST != null) {
        try {
          return (int) GET_MAX_COST.invoke(enchantment, level);
        } catch (Throwable ignored) {
          // This shouldn't be possible unless the implementation is modified to throw a runtime exception here.
        }
      }
      // Default to common rarity equivalent.
      return 6 + level * 10;
    };
  }

  static @Nullable Tag<Material> getPrimaryItems(Enchantment enchantment) {
    return getTagFor(enchantment, GET_PRIMARY_ITEMS);
  }

  static @NotNull Tag<Material> getSecondaryItems(Enchantment enchantment) {
    return Objects.requireNonNullElse(getTagFor(enchantment, GET_SECONDARY_ITEMS), ItemUtil.TAG_EMPTY);
  }

  private static @Nullable Tag<Material> getTagFor(Enchantment enchantment, MethodHandle items) {
    Object itemsRegistryKeySet;
    try {
      itemsRegistryKeySet = items.invoke(enchantment);
    } catch (Throwable e) {
      return ItemUtil.TAG_EMPTY;
    }

    if (itemsRegistryKeySet == null) {
      return null;
    }

    // For Spigot compatibility, wrap the returned RegistryKeySet in a dummy tag.
    // TODO: may be better to convert internal tag usage to a Predicate<Material>?
    return new Tag<>() {
      @Override
      public boolean isTagged(@NotNull Material item) {
        ItemType itemType = item.asItemType();
        if (itemType == null) {
          return false;
        }
        try {
          return (boolean) REGISTRY_KEY_SET_CONTAINS.invoke(itemsRegistryKeySet, itemType);
        } catch (Throwable e) {
          return false;
        }
      }

      @Override
      public @NotNull Set<Material> getValues() {
        return Set.of();
      }

      @Override
      public @NotNull NamespacedKey getKey() {
        return Objects.requireNonNull(NamespacedKey.fromString("planarenchanting:compat"));
      }
    };
  }

  static boolean isTreasure(Enchantment enchantment) {
    return TREASURE != null && TREASURE.isTagged(enchantment);
  }

  private EnchantDataPaper() {}

}
