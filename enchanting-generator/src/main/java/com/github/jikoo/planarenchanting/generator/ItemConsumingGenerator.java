package com.github.jikoo.planarenchanting.generator;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class ItemConsumingGenerator extends Generator {

  public ItemConsumingGenerator(String pkg, String name) {
    super(pkg, "Baked" + name + "Data");
  }

  public abstract void processItem(Identifier key, Item item);

}
