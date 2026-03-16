package com.github.jikoo.planarenchanting.anvil;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Repairable;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jspecify.annotations.NullMarked;

/**
 * Data component-based {@link AnvilBehavior}.
 */
@NullMarked
public class ComponentVanillaBehavior implements AnvilBehavior<ItemStack> {

  @Override
  public boolean enchantApplies(Enchantment enchantment, ItemStack base) {
    return enchantment.canEnchantItem(base);
  }

  @Override
  public boolean itemsCombineEnchants(ItemStack base, ItemStack addition) {
    return base.getType() == addition.getType() || addition.getType() == Material.ENCHANTED_BOOK;
  }

  @Override
  public boolean itemRepairedBy(ItemStack repaired, ItemStack repairMat) {
    Repairable repairable = repaired.getData(DataComponentTypes.REPAIRABLE);

    if (repairable == null) {
      return false;
    }

    // Note: KeyImpl and NamespacedKey have the same hashCode implementation, so
    // the fact that ItemTypeKeys all have a KeyImpl instead shouldn't matter.
    TypedKey<ItemType> itemKey = TypedKey.create(RegistryKey.ITEM, repairMat.getType().getKey());
    return repairable.types().contains(itemKey);
  }

}
