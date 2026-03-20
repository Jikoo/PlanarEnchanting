package com.github.jikoo.planarenchanting.util;

import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;
import com.github.jikoo.planarenchanting.util.EnchantData.Provider;
import org.bukkit.enchantments.Enchantment;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class MockEnchantDataProvider implements Provider {

  private Map<Enchantment, EnchantData> enchants = new HashMap<>();

  @Override
  public EnchantData of(Enchantment enchantment) {
    return enchants.computeIfAbsent(enchantment, ench -> mock());
  }

}
