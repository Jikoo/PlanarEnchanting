package com.github.jikoo.planarenchanting.anvil;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import com.github.jikoo.planarenchanting.util.EnchantData;
import com.github.jikoo.planarenchanting.util.EnchantmentAccess;
import org.bukkit.enchantments.Enchantment;
import org.jspecify.annotations.NullMarked;

/**
 * An {@link AnvilFunction} used to apply and combine enchantments.
 *
 * @param <T> the type of the input items
 */
@NullMarked
public class CombineEnchants<T> implements AnvilFunction<T> {

  private final EnchantingPlatform platform;
  private final EnchantmentAccess<T> access;

  protected CombineEnchants(Platform platform, EnchantmentAccess<T> access) {
    this.access = access;
    this.platform = platform.platform;
  }

  @Override
  public boolean canApply(AnvilBehavior<T> behavior, ViewState<T> state, T result) {
    return behavior.itemsCombineEnchants(state.getBase(), state.getAddition());
  }

  @Override
  public AnvilFunctionResult<T> getResult(AnvilBehavior<T> behavior, ViewState<T> state, T result) {
    Map<Enchantment, Integer> baseEnchants = access.getEnchantments(state.getBase());
    Map<Enchantment, Integer> additionEnchants = access.getEnchantments(state.getAddition());

    if (additionEnchants.isEmpty()) {
      return AnvilFunctionResult.empty();
    }

    MergeResult mergeResult = getLevelCost(
        behavior,
        state,
        baseEnchants,
        additionEnchants
    );

    int finalCost = mergeResult.levelCost < 0 ? state.getAnvilView().getMaximumRepairCost() : mergeResult.levelCost;

    return new AnvilFunctionResult<>() {
      @Override
      public int getLevelCostIncrease() {
        return finalCost;
      }

      @Override
      public void modifyResult(T t) {
        access.addEnchantments(t, mergeResult.enchantments);
      }
    };

  }

  /**
   * Produce a {@link MergeResult} for the combination of two sets of enchantments.
   *
   * @param behavior the {@link AnvilBehavior} controlling enchantment application
   * @param state the {@link ViewState} being operated on
   * @param baseEnchants the enchantments from the base item
   * @param additionEnchants the enchantments from the item being added and merged
   * @return the incurred level cost and resulting enchantments
   */
  protected MergeResult getLevelCost(
      AnvilBehavior<T> behavior,
      ViewState<T> state,
      Map<Enchantment, Integer> baseEnchants,
      Map<Enchantment, Integer> additionEnchants
  ) {
    Map<Enchantment, Integer> newEnchants = new HashMap<>(baseEnchants);
    int levelCost = 0;
    T base = state.getBase();
    boolean isFromBook = access.isBook(state.getAddition());

    for (Entry<Enchantment, Integer> enchantEntry : additionEnchants.entrySet()) {
      Enchantment newEnchantment = enchantEntry.getKey();
      if (behavior.enchantApplies(newEnchantment, base)
          && baseEnchants.keySet().stream()
              .noneMatch(existingEnchant ->
                  !Objects.equals(existingEnchant.getKey(), newEnchantment.getKey())
                      && behavior.enchantsConflict(existingEnchant, newEnchantment))) {
        int baseCost = platform.getAnvilCost(newEnchantment, isFromBook);
        int addedLevel = enchantEntry.getValue();
        int oldLevel = baseEnchants.getOrDefault(newEnchantment, 0);
        int newLevel = oldLevel == addedLevel ? addedLevel + 1 : Math.max(oldLevel, addedLevel);
        newLevel = Math.min(newLevel, behavior.getEnchantMaxLevel(newEnchantment));
        newEnchants.put(newEnchantment, newLevel);

        levelCost += platform.getTotalCost(baseCost, oldLevel, newLevel);
      } else {
        levelCost += platform.getInapplicableCost();
      }
    }
    return new MergeResult(levelCost, newEnchants);
  }

  protected record MergeResult(int levelCost, Map<Enchantment, Integer> enchantments) {}

  public enum Platform {
    JAVA(new Java()),
    BEDROCK(new Bedrock()),;

    private final EnchantingPlatform platform;

    Platform(EnchantingPlatform platform) {
      this.platform = platform;
    }
  }

  private sealed interface EnchantingPlatform {
    int getAnvilCost(Enchantment enchantment, boolean isFromBook);

    int getTotalCost(int baseCost, int oldLevel, int newLevel);

    int getInapplicableCost();
  }

  private static final class Java implements EnchantingPlatform {
    @Override
    public int getAnvilCost(Enchantment enchantment, boolean isFromBook) {
      int value = EnchantData.Service.PROVIDER.of(enchantment).getAnvilCost();
      return isFromBook ? Math.max(1, value / 2) : value;
    }

    @Override
    public int getTotalCost(int baseCost, int oldLevel, int newLevel) {
      return baseCost * newLevel;
    }

    @Override
    public int getInapplicableCost() {
      return 1;
    }
  }

  private static final class Bedrock implements EnchantingPlatform {
    @Override
    public int getAnvilCost(Enchantment enchantment, boolean isFromBook) {
      EnchantData data = EnchantData.Service.PROVIDER.of(enchantment);

      int cost = data.getAnvilCost();

      if (isFromBook) {
        cost /= 2;
      }

      if (data.isTridentEnchant()) {
        // Bedrock Edition rarity is 1 tier lower for trident enchantments.
        cost /= 2;
      }

      return Math.max(1, cost);
    }

    @Override
    public int getTotalCost(int baseCost, int oldLevel, int newLevel) {
      if (oldLevel >= newLevel) {
        return 0;
      }

      return baseCost * (newLevel - oldLevel);
    }

    @Override
    public int getInapplicableCost() {
      return 0;
    }

  }

}
