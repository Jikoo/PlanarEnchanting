package com.github.jikoo.planarenchanting.util;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Enchantable;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import io.papermc.paper.datacomponent.item.Repairable;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistrySet;
import java.util.Map;
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

final class ComponentCapability {

  // Providing a getter rather than a constant allows us to do a static mock.
  static boolean get() {
    try {
      return Wrapper.get();
    } catch (LinkageError e) {
      return false;
    }
  }

  // Inner class allows us to catch the exception in the outer class.
  // The only alternatives are not using a static helper method (which looks terrible)
  // or not catching the errors internally.
  private static final class Wrapper {

    static boolean get() {
      ItemStack itemStack = new ItemStack(Material.DIAMOND_PICKAXE);
      test(itemStack, DataComponentTypes.MAX_DAMAGE, () -> 50);
      test(itemStack, DataComponentTypes.DAMAGE, () -> 50);
      test(itemStack, DataComponentTypes.REPAIR_COST, () -> 50);
      test(itemStack, DataComponentTypes.REPAIRABLE, () -> Repairable.repairable(RegistrySet.keySet(RegistryKey.ITEM)));
      test(itemStack, DataComponentTypes.CUSTOM_NAME, () -> Component.text("Sample text"));
      test(itemStack, DataComponentTypes.ITEM_NAME, () -> Component.text("Sample text"));
      test(itemStack, DataComponentTypes.ENCHANTMENTS, () -> ItemEnchantments.itemEnchantments(Map.of()));
      test(itemStack, DataComponentTypes.STORED_ENCHANTMENTS, () -> ItemEnchantments.itemEnchantments(Map.of()));
      test(itemStack, DataComponentTypes.ENCHANTABLE, () -> Enchantable.enchantable(5));
      return true;
    }

    private static <T> void test(ItemStack itemStack, DataComponentType.Valued<T> type, Supplier<T> value) {
      T data = itemStack.getData(type);

      if (data == null) {
        data = value.get();
      }

      itemStack.setData(type, data);
    }

    private Wrapper() {}

  }

  private ComponentCapability() {}

}
