package com.github.jikoo.planarenchanting.anvil;

import com.github.jikoo.planarenchanting.enchant.EnchantData;
import com.github.jikoo.planarenchanting.enchant.EnchantmentUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class CombineEnchantments implements AnvilFunction {

  @Override
  public boolean canApply(@NotNull AnvilBehavior behavior, @NotNull AnvilState state) {
    return behavior.itemsCombineEnchants(state.getBase(), state.getAddition());
  }

  @Override
  public final @NotNull AnvilFunctionResult getResult(
      @NotNull AnvilBehavior behavior,
      @NotNull AnvilState state) {
    Map<Enchantment, Integer> baseEnchants = EnchantmentUtil.getEnchants(
        state.getBase().getMeta());
    Map<Enchantment, Integer> additionEnchants = EnchantmentUtil.getEnchants(
        state.getAddition().getMeta());

    if (additionEnchants.isEmpty()) {
      return AnvilFunctionResult.EMPTY;
    }

    Map<Enchantment, Integer> newEnchants = new HashMap<>(baseEnchants);
    boolean isFromBook = state.getAddition().getItem().getType() == Material.ENCHANTED_BOOK;

    int levelCost = 0;
    for (Entry<Enchantment, Integer> enchantEntry : additionEnchants.entrySet()) {
      Enchantment newEnchantment = enchantEntry.getKey();
      int oldLevel = baseEnchants.getOrDefault(newEnchantment, 0);
      int baseCost = getAnvilCost(newEnchantment, isFromBook);
      if (behavior.enchantApplies(newEnchantment, state.getBase())
          && baseEnchants.keySet().stream()
              .noneMatch(existingEnchant ->
                  !existingEnchant.getKey().equals(newEnchantment.getKey())
                      && behavior.enchantsConflict(existingEnchant, newEnchantment))) {

        int addedLevel = enchantEntry.getValue();
        int newLevel = oldLevel == addedLevel ? addedLevel + 1 : Math.max(oldLevel, addedLevel);
        newLevel = Math.min(newLevel, behavior.getEnchantMaxLevel(newEnchantment));
        newEnchants.put(newEnchantment, newLevel);

        levelCost += getTotalCost(baseCost, oldLevel, newLevel);
      } else {
        levelCost += getNonApplicableCost();
      }
    }

    if (levelCost < 0) {
      levelCost = state.getAnvil().getMaximumRepairCost();
    }

    int totalLevelCost = levelCost;

    return new AnvilFunctionResult() {
      @Override
      public int getLevelCostIncrease() {
        return totalLevelCost;
      }

      @Override
      public void modifyResult(@Nullable ItemMeta itemMeta) {
        EnchantmentUtil.addEnchants(itemMeta, newEnchants);
      }
    };

  }

  protected int getAnvilCost(Enchantment enchantment, boolean isFromBook) {
    int value = EnchantData.of(enchantment).getAnvilCost();
    return isFromBook ? Math.max(1, value / 2) : value;
  }

  protected abstract int getTotalCost(int baseCost, int oldLevel, int newLevel);

  protected abstract int getNonApplicableCost();

}
