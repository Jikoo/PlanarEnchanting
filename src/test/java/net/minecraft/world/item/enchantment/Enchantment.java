package net.minecraft.world.item.enchantment;

import org.bukkit.NamespacedKey;
import java.util.function.IntUnaryOperator;

public record Enchantment(
    NamespacedKey key,
    IntUnaryOperator minCost,
    IntUnaryOperator maxCost,
    // NMSREF \nnet\.minecraft\.world\.item\.enchantment\.Enchantment(.|\n)*?int getWeight\(\)
    int d,
    // NMSREF \nnet\.minecraft\.world\.item\.enchantment\.Enchantment(.|\n)*?int getAnvilCost\(\)
    int e) {

  // NMSREF \nnet\.minecraft\.world\.item\.enchantment\.Enchantment(.|\n)*?int getMinCost\(int\)
  public int c(int level) {
    return minCost().applyAsInt(level);
  }

  // NMSREF \nnet\.minecraft\.world\.item\.enchantment\.Enchantment(.|\n)*?int getMaxCost\(int\)
  public int d(int level) {
    return maxCost().applyAsInt(level);
  }

}
