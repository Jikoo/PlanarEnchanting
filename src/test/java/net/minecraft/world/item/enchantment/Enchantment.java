package net.minecraft.world.item.enchantment;

import java.util.function.IntUnaryOperator;
import org.bukkit.NamespacedKey;

public record Enchantment(
    NamespacedKey key,
    IntUnaryOperator minCost,
    IntUnaryOperator maxCost,
    // NMSREF \nnet\.minecraft\.world\.item\.enchantment\.Enchantment(.|\n)*?int getWeight\(\)
    int b,
    // NMSREF \nnet\.minecraft\.world\.item\.enchantment\.Enchantment(.|\n)*?int getAnvilCost\(\)
    int c) {

  // NMSREF \nnet\.minecraft\.world\.item\.enchantment\.Enchantment(.|\n)*?int getMinCost\(int\)
  public int b(int level) {
    return minCost().applyAsInt(level);
  }

  // NMSREF \nnet\.minecraft\.world\.item\.enchantment\.Enchantment(.|\n)*?int getMaxCost\(int\)
  public int c(int level) {
    return maxCost().applyAsInt(level);
  }

}
