package net.minecraft.core;

import org.bukkit.enchantments.Enchantment;

public interface IRegistry<T> {

  // NMSREF \nnet\.minecraft\.core\.Registry(.|\n)*?net\.minecraft\.core\.Registry ENCHANTMENT
  IRegistry<Enchantment> V = object -> {
    Enchantment[] enchantments = Enchantment.values();
    for (int i = 0; i < enchantments.length; i++) {
      if (enchantments[i].equals(object)) {
        return i;
      }
    }
    return enchantments.length;
  };

  // NMSREF \nnet\.minecraft\.core\.Registry(.|\n)*?int getId\(java\.lang\.Object\)
  int a(T object);

}
