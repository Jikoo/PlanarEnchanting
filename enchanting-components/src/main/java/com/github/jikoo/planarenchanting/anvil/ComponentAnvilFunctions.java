package com.github.jikoo.planarenchanting.anvil;

import static io.papermc.paper.datacomponent.DataComponentTypes.CUSTOM_NAME;
import static io.papermc.paper.datacomponent.DataComponentTypes.DAMAGE;
import static io.papermc.paper.datacomponent.DataComponentTypes.MAX_DAMAGE;
import static io.papermc.paper.datacomponent.DataComponentTypes.REPAIR_COST;

import io.papermc.paper.datacomponent.DataComponentType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

/**
 * Data component-based {@link AnvilFunctionsProvider}.
 */
@NullMarked
public final class ComponentAnvilFunctions implements AnvilFunctionsProvider<ItemStack> {

  public static final AnvilFunction<ItemStack> PRIOR_WORK_LEVEL_COST = new AnvilFunction<>() {
    @Override
    public boolean canApply(
        AnvilBehavior<ItemStack> behavior,
        ViewState<ItemStack> state,
        ItemStack result
    ) {
      return true;
    }

    @Override
    public AnvilFunctionResult<ItemStack> getResult(
        AnvilBehavior<ItemStack> behavior,
        ViewState<ItemStack> state,
        ItemStack result
    ) {
      return new AnvilFunctionResult<>() {
        @Override
        public int getLevelCostIncrease() {
          return get(state.getBase(), REPAIR_COST) + get(state.getAddition(), REPAIR_COST);
        }
      };
    }
  };
  public static final AnvilFunction<ItemStack> RENAME = new AnvilFunction<>() {
    @Override
    public boolean canApply(
        AnvilBehavior<ItemStack> behavior,
        ViewState<ItemStack> state,
        ItemStack result
    ) {
      Component data = state.getBase().getData(CUSTOM_NAME);
      String anvilText = state.getAnvilView().getRenameText();

      // If the names aren't the same, the rename can be applied.
      if (data == null) {
        return anvilText != null && !anvilText.isEmpty();
      }

      return !LegacyComponentSerializer.legacySection().serialize(data).equals(anvilText);
    }

    @Override
    public AnvilFunctionResult<ItemStack> getResult(
        AnvilBehavior<ItemStack> behavior,
        ViewState<ItemStack> state,
        ItemStack result
    ) {
      return new AnvilFunctionResult<>() {
        @Override
        public int getLevelCostIncrease() {
          return 1;
        }

        @Override
        public void modifyResult(ItemStack modified) {
          String anvilText = state.getAnvilView().getRenameText();

          if (anvilText == null || anvilText.isEmpty()) {
            modified.resetData(CUSTOM_NAME);
          } else {
            modified.setData(CUSTOM_NAME, Component.text(anvilText));
          }

          int priorCost = Math.max(get(state.getBase(), REPAIR_COST), get(state.getAddition(), REPAIR_COST));
          modified.setData(REPAIR_COST, priorCost);
        }
      };
    }
  };
  public static final AnvilFunction<ItemStack> UPDATE_PRIOR_WORK_COST = new AnvilFunction<>() {
    @Override
    public boolean canApply(
        AnvilBehavior<ItemStack> behavior,
        ViewState<ItemStack> state,
        ItemStack result
    ) {
      return true;
    }

    @Override
    public AnvilFunctionResult<ItemStack> getResult(
        AnvilBehavior<ItemStack> behavior,
        ViewState<ItemStack> state,
        ItemStack result
    ) {
      return new AnvilFunctionResult<>() {
        @Override
        public void modifyResult(ItemStack modified) {
          int priorCost = Math.max(
              get(state.getBase(), REPAIR_COST),
              get(state.getAddition(), REPAIR_COST)
          );
          modified.setData(REPAIR_COST, priorCost * 2 + 1);
        }
      };
    }
  };
  public static final AnvilFunction<ItemStack> REPAIR_WITH_MATERIAL = new AnvilFunction<>() {
    @Override
    public boolean canApply(
        AnvilBehavior<ItemStack> behavior,
        ViewState<ItemStack> state,
        ItemStack result
    ) {
      return get(state.getBase(), DAMAGE) > 0
          && behavior.itemRepairedBy(state.getBase(), state.getAddition());
    }

    @Override
    public AnvilFunctionResult<ItemStack> getResult(
        AnvilBehavior<ItemStack> behavior,
        ViewState<ItemStack> state,
        ItemStack result
    ) {
      int damage = get(state.getBase(), DAMAGE);
      if (damage <= 0) {
        return AnvilFunctionResult.empty();
      }

      int maxDamage = get(state.getBase(), MAX_DAMAGE);
      if (maxDamage <= 0) {
        return AnvilFunctionResult.empty();
      }

      int repairPerMaterial = maxDamage / 4;
      int repairsNeeded = Math.ceilDiv(damage, repairPerMaterial);

      final int repairsAvailable = Math.min(repairsNeeded, state.getAddition().getAmount());
      final int resultDamage = Math.max(0, damage - (repairsAvailable * repairPerMaterial));

      return new AnvilFunctionResult<>() {
        @Override
        public int getLevelCostIncrease() {
          return repairsAvailable;
        }

        @Override
        public int getMaterialCostIncrease() {
          return repairsAvailable;
        }

        @Override
        public void modifyResult(ItemStack modified) {
          modified.setData(DAMAGE, resultDamage);
        }
      };
    }
  };
  public static final AnvilFunction<ItemStack> REPAIR_WITH_COMBINATION = new AnvilFunction<>() {
    @Override
    public boolean canApply(
        AnvilBehavior<ItemStack> behavior,
        ViewState<ItemStack> state,
        ItemStack result
    ) {
      if (state.getBase().getType() != state.getAddition().getType()) {
        return false;
      }
      // If either is undamaged, not eligible for repair.
      if (get(state.getBase(), DAMAGE) <= 0) {
        return false;
      }
      // If the base doesn't have a valid max damage, not eligible.
      Integer baseMaxDamage = state.getBase().getData(MAX_DAMAGE);
      if (baseMaxDamage == null || baseMaxDamage <= 0) {
        return false;
      }
      // If the base and addition mismatch for some reason, they're probably secretly
      // different, and we don't really want to open that can of worms.
      return baseMaxDamage.equals(state.getAddition().getData(MAX_DAMAGE));
    }

    @Override
    public AnvilFunctionResult<ItemStack> getResult(
        AnvilBehavior<ItemStack> behavior,
        ViewState<ItemStack> state,
        ItemStack result
    ) {
      int damage = get(state.getBase(), DAMAGE);

      if (damage <= 0) {
        return AnvilFunctionResult.empty();
      }

      int maxDamage = get(state.getBase(), MAX_DAMAGE);

      if (maxDamage <= 0) {
        return AnvilFunctionResult.empty();
      }

      int restored = (int) (maxDamage - get(state.getAddition(), DAMAGE) + maxDamage * 0.12);

      final int resultDamage = Math.max(0, damage - restored);

      return new AnvilFunctionResult<>() {
        @Override
        public int getLevelCostIncrease() {
          return 2;
        }

        @Override
        public void modifyResult(ItemStack modified) {
          modified.setData(DAMAGE, resultDamage);
        }
      };
    }
  };
  public static final AnvilFunction<ItemStack> COMBINE_ENCHANTMENTS_JAVA;
  public static final AnvilFunction<ItemStack> COMBINE_ENCHANTMENTS_BEDROCK;

  public static final ComponentAnvilFunctions INSTANCE = new ComponentAnvilFunctions();

  static {
    ComponentEnchantmentAccess access = new ComponentEnchantmentAccess();
    COMBINE_ENCHANTMENTS_JAVA = new CombineEnchants<>(CombineEnchants.Platform.JAVA, access);
    COMBINE_ENCHANTMENTS_BEDROCK = new CombineEnchants<>(CombineEnchants.Platform.BEDROCK, access);
  }

  private ComponentAnvilFunctions() {}

  @Override
  public AnvilFunction<ItemStack> addPriorWorkLevelCost() {
    return PRIOR_WORK_LEVEL_COST;
  }

  @Override
  public AnvilFunction<ItemStack> rename() {
    return RENAME;
  }

  @Override
  public AnvilFunction<ItemStack> setItemPriorWork() {
    return UPDATE_PRIOR_WORK_COST;
  }

  @Override
  public AnvilFunction<ItemStack> repairWithMaterial() {
    return REPAIR_WITH_MATERIAL;
  }

  @Override
  public AnvilFunction<ItemStack> repairWithCombine() {
    return REPAIR_WITH_COMBINATION;
  }

  @Override
  public AnvilFunction<ItemStack> combineEnchantsJava() {
    return COMBINE_ENCHANTMENTS_JAVA;
  }

  @Override
  public AnvilFunction<ItemStack> combineEnchantsBedrock() {
    return COMBINE_ENCHANTMENTS_BEDROCK;
  }

  private static int get(ItemStack itemStack, DataComponentType.Valued<Integer> type) {
    Integer data = itemStack.getData(type);
    return data != null ? data : 0;
  }

}
