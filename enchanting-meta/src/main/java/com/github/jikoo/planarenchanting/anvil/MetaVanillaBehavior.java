package com.github.jikoo.planarenchanting.anvil;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class MetaVanillaBehavior implements AnvilBehavior<MetaCachedStack> {

  @Override
  public boolean enchantApplies(Enchantment enchantment, MetaCachedStack base) {
    return enchantment.canEnchantItem(base.getItem());
  }

  @Override
  public boolean itemsCombineEnchants(MetaCachedStack base, MetaCachedStack addition) {
    Material addedType = addition.getItem().getType();
    return base.getItem().getType() == addedType || addedType == Material.ENCHANTED_BOOK;
  }

  @Override
  public boolean itemRepairedBy(MetaCachedStack repaired, MetaCachedStack repairMat) {
    return RepairMaterial.repairs(repaired.getItem(), repairMat.getItem());
  }

}
