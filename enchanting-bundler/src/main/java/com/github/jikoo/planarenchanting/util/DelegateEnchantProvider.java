package com.github.jikoo.planarenchanting.util;

import com.github.jikoo.planarenchanting.util.EnchantData.Provider;
import org.bukkit.enchantments.Enchantment;
import org.jspecify.annotations.NullMarked;

@NullMarked
class DelegateEnchantProvider implements Provider {

  private final Provider delegate =
      ServerCapabilities.DATA_COMPONENT ? new ComponentEnchantProvider() : new MetaEnchantProvider();

  @Override
  public EnchantData of(Enchantment enchantment) {
    return delegate.of(enchantment);
  }

}
