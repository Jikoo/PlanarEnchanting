package net.minecraft.world.item.enchantment;

import java.util.function.IntUnaryOperator;

public record Enchantment(
    IntUnaryOperator minCost,
    IntUnaryOperator maxCost,
    Rarity d) {

  // NMSREF \nnet\.minecraft\.world\.item\.enchantment\.Enchantment(.|\n)*?int getMinCost\(int\)
  public int a(int level) {
    return minCost().applyAsInt(level);
  }

  // NMSREF \nnet\.minecraft\.world\.item\.enchantment\.Enchantment(.|\n)*?int getMaxCost\(int\)
  public int b(int level) {
    return maxCost().applyAsInt(level);
  }

  // NMSREF \nnet\.minecraft\.world\.item\.enchantment\.Enchantment\$Rarity(.|\n)*?int getWeight\(\)
  public record Rarity(int a) {}

}
