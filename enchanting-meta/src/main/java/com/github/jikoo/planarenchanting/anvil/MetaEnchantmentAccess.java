package com.github.jikoo.planarenchanting.anvil;

import com.github.jikoo.planarenchanting.util.EnchantmentAccess;
import java.util.Map;
import java.util.function.BiConsumer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NullMarked;

@NullMarked
class MetaEnchantmentAccess implements EnchantmentAccess<MetaCachedStack> {

  @Override
  public boolean isBook(MetaCachedStack metaCachedStack) {
    return metaCachedStack.getItem().getType() == Material.ENCHANTED_BOOK;
  }

  @Override
  public Map<Enchantment, Integer> getEnchantments(MetaCachedStack metaCachedStack) {
    if (metaCachedStack.getMeta() instanceof EnchantmentStorageMeta storageMeta) {
      return storageMeta.getStoredEnchants();
    }
    ItemMeta itemMeta = metaCachedStack.getMeta();
    return itemMeta != null ? itemMeta.getEnchants() : Map.of();
  }

  @Override
  public void addEnchantments(MetaCachedStack metaCachedStack, Map<Enchantment, Integer> enchantments) {
    BiConsumer<Enchantment, Integer> consumer;
    if (metaCachedStack.getMeta() instanceof EnchantmentStorageMeta storageMeta) {
      consumer = (ench, lvl) -> storageMeta.addStoredEnchant(ench, lvl, true);
    } else {
      ItemMeta itemMeta = metaCachedStack.getMeta();
      if (itemMeta == null) {
        return;
      }
      consumer = (ench, lvl) -> itemMeta.addEnchant(ench, lvl, true);
    }

    enchantments.forEach(consumer);
  }

}
