package com.github.jikoo.planarenchanting.util;

import com.github.jikoo.planarenchanting.util.EnchantData.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;

/**
 * An {@link Provider EnchantData.Provider} using pre-baked enchantment data. May not include more
 * recently added enchantments! Falls through to data for unbreaking.
 */
@Internal
@NullMarked
public class MetaEnchantProvider implements Provider {

  private final Map<NamespacedKey, EnchantData> data = new HashMap<>();

  MetaEnchantProvider() {
    load();
  }

  private void load() {
    Registry<Enchantment> registry = Objects.requireNonNull(Bukkit.getRegistry(Enchantment.class));
    for (var entry : BakedEnchantData.get().entrySet()) {
      if (entry.getKey() == null) {
        continue;
      }

      Enchantment enchant = registry.get(entry.getKey());

      if (enchant != null) {
        data.put(enchant.getKey(), entry.getValue().apply(enchant));
      }
    }
  }

  @Override
  public EnchantData of(Enchantment enchantment) {
    return data.computeIfAbsent(
        enchantment.getKey(),
        // Since Spigot lacks APIs for most of these areas, model defaults off of Unbreaking.
        // It's relatively middle-of-the-road across the board.
        key -> BakedEnchantData.create(
            5,
            2,
            lvl -> 5 + 8 * (lvl - 1),
            lvl -> 55 + 8 * (lvl - 1)
        ).apply(enchantment)
    );
  }

}
