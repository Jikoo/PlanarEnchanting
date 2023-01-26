package net.minecraft.core;

import net.minecraft.world.item.enchantment.Enchantment;

public interface IRegistry<T> {

  // NMSREF \nnet\.minecraft\.core\.Registry(.|\n)*?net\.minecraft\.core\.Registry ENCHANTMENT
  IRegistry<Enchantment> W = object -> {
    org.bukkit.enchantments.Enchantment[] enchantments = org.bukkit.enchantments.Enchantment.values();
    for (int i = 0; i < enchantments.length; i++) {
      if (enchantments[i].getKey().equals(object.key())) {
        return i;
      }
    }
    return enchantments.length;
  };

  // NMSREF \nnet\.minecraft\.core\.Registry(.|\n)*?int getId\(java\.lang\.Object\)
  int a(T object);

}
