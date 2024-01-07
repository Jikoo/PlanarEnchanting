package com.github.jikoo.planarenchanting.util.mock.enchantments;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Server;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.mockito.ArgumentMatchers;

public class EnchantmentMocks {
  private static final Map<NamespacedKey, Enchantment> KEYS_TO_ENCHANTS = new HashMap<>();

  public static void init(Server server) {
    // Horrible load order mess:
    // Enchantments load as soon as the Enchantment class is initialized, which happens when
    // Registry.ENCHANTMENT is initialized during Registry initialization. This means that we must
    // be prepared to initialize enchantments before we can initialize the enchantment registry at
    // all, and can reference neither class until then.
    // This solution is not compatible with all registries (i.e. structures) and may need to be revisited.
    doAnswer(invocationGetRegistry -> {
      Registry<?> registry = mock(Registry.class);
      // This must be mocked here or else Registry will be initialized when mocking it.
      doAnswer(invocationGetEntry -> {
        NamespacedKey key = invocationGetEntry.getArgument(0);
        // Set registry to always return a new enchantment mock.
        // This allows us to use Bukkit's up-to-date namespaced keys instead of maintaining a listing.
        if (invocationGetRegistry.getArgument(0).equals(Enchantment.class)) {
          Enchantment enchant = mock(Enchantment.class);
          doReturn(key).when(enchant).getKey();
          doReturn(key.getKey()).when(enchant).getName();
          return enchant;
        }
        // Other unsupported registry type. Try for compatibility with Keyed return, though it will
        // probably result in a ClassCastException for most uses.
        return new Keyed() {
          @NotNull
          @Override
          public NamespacedKey getKey() {
            return key;
          }
        };
      }).when(registry).get(notNull());
      return registry;
    }).when(server).getRegistry(notNull());

    Registry<?> registry = Registry.ENCHANTMENT;
    // Redirect enchantment registry back so that our modified version is always returned.
    doReturn(registry).when(server).getRegistry(Enchantment.class);

    setUpEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4, EnchantmentTarget.ARMOR,
        List.of(Enchantment.PROTECTION_FIRE, Enchantment.PROTECTION_EXPLOSIONS, Enchantment.PROTECTION_PROJECTILE));
    setUpEnchant(Enchantment.PROTECTION_FIRE, 4, EnchantmentTarget.ARMOR,
        List.of(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_EXPLOSIONS, Enchantment.PROTECTION_PROJECTILE));
    setUpEnchant(Enchantment.PROTECTION_FALL, 4, EnchantmentTarget.ARMOR_FEET);
    setUpEnchant(Enchantment.PROTECTION_EXPLOSIONS, 4, EnchantmentTarget.ARMOR,
        List.of(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_FIRE, Enchantment.PROTECTION_PROJECTILE));
    setUpEnchant(Enchantment.PROTECTION_PROJECTILE, 4, EnchantmentTarget.ARMOR,
        List.of(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_FIRE, Enchantment.PROTECTION_EXPLOSIONS));

    setUpEnchant(Enchantment.OXYGEN, 3, EnchantmentTarget.ARMOR_HEAD);
    setUpEnchant(Enchantment.WATER_WORKER, 1, EnchantmentTarget.ARMOR_HEAD);

    setUpEnchant(Enchantment.THORNS, 3, EnchantmentTarget.ARMOR);

    setUpEnchant(Enchantment.DEPTH_STRIDER, 3, EnchantmentTarget.ARMOR_FEET, List.of(Enchantment.FROST_WALKER));
    setUpEnchant(Enchantment.FROST_WALKER, 3, EnchantmentTarget.ARMOR_FEET, true, false, List.of(Enchantment.DEPTH_STRIDER));

    setUpEnchant(Enchantment.BINDING_CURSE, 1, EnchantmentTarget.WEARABLE, true, true, List.of());

    setUpEnchant(Enchantment.DAMAGE_ALL, 5, EnchantmentTarget.WEAPON, List.of(Enchantment.DAMAGE_ARTHROPODS, Enchantment.DAMAGE_UNDEAD));
    setUpEnchant(Enchantment.DAMAGE_UNDEAD, 5, EnchantmentTarget.WEAPON, List.of(Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_ARTHROPODS));
    setUpEnchant(Enchantment.DAMAGE_ARTHROPODS, 5, EnchantmentTarget.WEAPON, List.of(Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_UNDEAD));
    setUpEnchant(Enchantment.KNOCKBACK, 2, EnchantmentTarget.WEAPON);
    setUpEnchant(Enchantment.FIRE_ASPECT, 2, EnchantmentTarget.WEAPON);
    setUpEnchant(Enchantment.LOOT_BONUS_MOBS, 3, EnchantmentTarget.WEAPON);
    setUpEnchant(Enchantment.SWEEPING_EDGE, 3, EnchantmentTarget.WEAPON);

    setUpEnchant(Enchantment.DIG_SPEED, 5, EnchantmentTarget.TOOL);
    setUpEnchant(Enchantment.SILK_TOUCH, 1, EnchantmentTarget.TOOL, List.of(Enchantment.LOOT_BONUS_BLOCKS));

    setUpEnchant(Enchantment.DURABILITY, 3, EnchantmentTarget.BREAKABLE);

    setUpEnchant(Enchantment.LOOT_BONUS_BLOCKS, 3, EnchantmentTarget.TOOL, List.of(Enchantment.SILK_TOUCH));

    setUpEnchant(Enchantment.ARROW_DAMAGE, 5, EnchantmentTarget.BOW);
    setUpEnchant(Enchantment.ARROW_KNOCKBACK, 2, EnchantmentTarget.BOW);
    setUpEnchant(Enchantment.ARROW_FIRE, 1, EnchantmentTarget.BOW);
    setUpEnchant(Enchantment.ARROW_INFINITE, 1, EnchantmentTarget.BOW, List.of(Enchantment.MENDING));

    setUpEnchant(Enchantment.LUCK, 3, EnchantmentTarget.FISHING_ROD);
    setUpEnchant(Enchantment.LURE, 3, EnchantmentTarget.FISHING_ROD);

    setUpEnchant(Enchantment.LOYALTY, 3, EnchantmentTarget.TRIDENT, List.of(Enchantment.RIPTIDE));
    setUpEnchant(Enchantment.IMPALING, 5, EnchantmentTarget.TRIDENT);
    setUpEnchant(Enchantment.RIPTIDE, 3, EnchantmentTarget.TRIDENT, List.of(Enchantment.CHANNELING, Enchantment.LOYALTY));
    setUpEnchant(Enchantment.CHANNELING, 1, EnchantmentTarget.TRIDENT, List.of(Enchantment.RIPTIDE));

    setUpEnchant(Enchantment.MULTISHOT, 1, EnchantmentTarget.CROSSBOW, List.of(Enchantment.PIERCING));
    setUpEnchant(Enchantment.QUICK_CHARGE, 3, EnchantmentTarget.CROSSBOW);
    setUpEnchant(Enchantment.PIERCING, 4, EnchantmentTarget.CROSSBOW, List.of(Enchantment.MULTISHOT));

    setUpEnchant(Enchantment.MENDING, 1, EnchantmentTarget.BREAKABLE, List.of(Enchantment.ARROW_INFINITE));

    setUpEnchant(Enchantment.VANISHING_CURSE, 1, EnchantmentTarget.VANISHABLE, true, true, List.of());

    setUpEnchant(Enchantment.SOUL_SPEED, 3, EnchantmentTarget.ARMOR_FEET, true, false, List.of());
    setUpEnchant(Enchantment.SWIFT_SNEAK, 3, EnchantmentTarget.ARMOR_LEGS, true, false, List.of());

    Set<String> missingInternalEnchants = new HashSet<>();
    try {
      for (Field field : Enchantment.class.getFields()) {
        if (Modifier.isStatic(field.getModifiers()) && Enchantment.class.equals(field.getType())) {
          Enchantment declaredEnchant = (Enchantment) field.get(null);
          if (!KEYS_TO_ENCHANTS.containsKey(declaredEnchant.getKey())) {
            missingInternalEnchants.add(declaredEnchant.getKey().toString());
          }
        }
      }
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }

    if (!missingInternalEnchants.isEmpty()) {
      throw new IllegalStateException("Missing enchantment declarations for " + missingInternalEnchants);
    }

    // When all enchantments are initialized using Bukkit keys, redirect registry to our map
    // so that invalid keys result in the expected null response.
    doAnswer(invocation -> KEYS_TO_ENCHANTS.get(invocation.getArgument(0, NamespacedKey.class)))
        .when(registry).get(ArgumentMatchers.notNull());
    doAnswer(invocation -> KEYS_TO_ENCHANTS.values().stream()).when(registry).stream();
    doAnswer(invocation -> KEYS_TO_ENCHANTS.values().iterator()).when(registry).iterator();
  }

  public static void putEnchant(@NotNull Enchantment enchantment) {
    KEYS_TO_ENCHANTS.put(enchantment.getKey(), enchantment);
  }

  private static void setUpEnchant(
      @NotNull Enchantment enchantment,
      int maxLevel,
      @NotNull EnchantmentTarget target) {
    setUpEnchant(enchantment, maxLevel, target, false, false, List.of());
  }

  private static void setUpEnchant(
      @NotNull Enchantment enchantment,
      int maxLevel,
      @NotNull EnchantmentTarget target,
      @NotNull Collection<Enchantment> conflicts) {
    setUpEnchant(enchantment, maxLevel, target, false, false, conflicts);
  }

  private static void setUpEnchant(
      @NotNull Enchantment enchantment,
      int maxLevel,
      @NotNull EnchantmentTarget target,
      boolean treasure,
      boolean curse,
      @NotNull Collection<Enchantment> conflicts) {
    KEYS_TO_ENCHANTS.put(enchantment.getKey(), enchantment);

    doReturn(1).when(enchantment).getStartLevel();
    doReturn(maxLevel).when(enchantment).getMaxLevel();
    doReturn(target).when(enchantment).getItemTarget();
    doAnswer(invocation -> {
      ItemStack item = invocation.getArgument(0);
      return item != null && target.includes(item);
    }).when(enchantment).canEnchantItem(any());
    doReturn(treasure).when(enchantment).isTreasure();
    doReturn(curse).when(enchantment).isCursed();
    // Note: Usual implementation of equals allows for simple contains check, but as these are
    // mocks that cannot be relied on.
    doAnswer(invocation -> conflicts.stream().anyMatch(conflict -> conflict.getKey().equals(invocation.getArgument(0, Enchantment.class).getKey())))
        .when(enchantment).conflictsWith(any());
  }

}
