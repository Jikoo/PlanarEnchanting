package com.github.jikoo.planarenchanting.anvil;

import org.bukkit.Material;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class MetaAnvilFunctions implements AnvilFunctionsProvider<MetaCachedStack> {

  public static final AnvilFunction<MetaCachedStack> PRIOR_WORK_LEVEL_COST = new AnvilFunction<>() {
    @Override
    public boolean canApply(
        AnvilBehavior<MetaCachedStack> behavior,
        ViewState<MetaCachedStack> state,
        MetaCachedStack result
    ) {
      return true;
    }

    @Override
    public AnvilFunctionResult<MetaCachedStack> getResult(
        AnvilBehavior<MetaCachedStack> behavior,
        ViewState<MetaCachedStack> state,
        MetaCachedStack result
    ) {
      return new AnvilFunctionResult<>() {
        @Override
        public int getLevelCostIncrease() {
          return getRepairCost(state.getBase().getMeta())
              + getRepairCost(state.getAddition().getMeta());
        }
      };
    }
  };
  public static final AnvilFunction<MetaCachedStack> RENAME = new AnvilFunction<>() {
    @Override
    public boolean canApply(
        AnvilBehavior<MetaCachedStack> behavior,
        ViewState<MetaCachedStack> state,
        MetaCachedStack result
    ) {
      var itemMeta = state.getBase().getMeta();

      if (itemMeta == null) {
        return false;
      }

      // If names are not the same, can be applied.
      String customName = itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : null;
      String anvilText = state.getOriginalView().getRenameText();
      if (customName == null) {
        return anvilText != null;
      }
      return !customName.equals(anvilText);
    }

    @Override
    public AnvilFunctionResult<MetaCachedStack> getResult(
        AnvilBehavior<MetaCachedStack> behavior,
        ViewState<MetaCachedStack> state,
        MetaCachedStack result
    ) {
      return new AnvilFunctionResult<>() {
        @Override
        public int getLevelCostIncrease() {
          // Renames always apply a level cost of 1.
          return 1;
        }

        @Override
        public void modifyResult(MetaCachedStack item) {
          ItemMeta itemMeta = item.getMeta();
          if (itemMeta == null) {
            return;
          }

          itemMeta.setDisplayName(state.getOriginalView().getRenameText());
          if (itemMeta instanceof Repairable repairable) {
            int repairCost = Math.max(
                getRepairCost(state.getBase().getMeta()),
                getRepairCost(state.getAddition().getMeta()));
            repairable.setRepairCost(repairCost);
          }
        }
      };
    }
  };
  public static final AnvilFunction<MetaCachedStack> UPDATE_PRIOR_WORK_COST = new AnvilFunction<>() {
    @Override
    public boolean canApply(
        AnvilBehavior<MetaCachedStack> behavior,
        ViewState<MetaCachedStack> state,
        MetaCachedStack result
    ) {
      return state.getBase().getMeta() instanceof Repairable;
    }

    @Override
    public AnvilFunctionResult<MetaCachedStack> getResult(
        AnvilBehavior<MetaCachedStack> behavior,
        ViewState<MetaCachedStack> state,
        MetaCachedStack result
    ) {
      return new AnvilFunctionResult<>() {
        @Override
        public void modifyResult(MetaCachedStack item) {
          if (item.getMeta() instanceof Repairable repairable) {
            int priorRepairCost = Math.max(
                getRepairCost(state.getBase().getMeta()),
                getRepairCost(state.getAddition().getMeta())
            );
            repairable.setRepairCost(priorRepairCost * 2 + 1);
          }
        }
      };
    }
  };
  public static final AnvilFunction<MetaCachedStack> REPAIR_WITH_MATERIAL = new AnvilFunction<>() {
    @Override
    public boolean canApply(
        AnvilBehavior<MetaCachedStack> behavior,
        ViewState<MetaCachedStack> state,
        MetaCachedStack result
    ) {
      MetaCachedStack base = state.getBase();
      return behavior.itemRepairedBy(base, state.getAddition())
          && base.getItem().getType().getMaxDurability() > 0
          && base.getMeta() instanceof Damageable damageable
          && damageable.getDamage() > 0;
    }

    @Override
    public AnvilFunctionResult<MetaCachedStack> getResult(
        AnvilBehavior<MetaCachedStack> behavior,
        ViewState<MetaCachedStack> state,
        MetaCachedStack result
    ) {
      if (!(state.getBase().getMeta() instanceof Damageable damageable)) {
        // If result is not damageable, it cannot be repaired.
        return AnvilFunctionResult.empty();
      }

      int missingDurability = damageable.getDamage();

      if (missingDurability < 1) {
        // If result is not damaged, no repair.
        return AnvilFunctionResult.empty();
      }

      int repairPerMaterial = state.getBase().getItem().getType().getMaxDurability() / 4;
      int repairsNeeded = Math.ceilDiv(missingDurability, repairPerMaterial);

      final int repairsAvailable = Math.min(repairsNeeded, state.getAddition().getItem().getAmount());
      final int resultDamage = Math.max(0, missingDurability - (repairsAvailable * repairPerMaterial));

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
        public void modifyResult(MetaCachedStack stack) {
          if (stack.getMeta() instanceof Damageable damageable) {
            damageable.setDamage(resultDamage);
          }
        }
      };
    }
  };
  public static final AnvilFunction<MetaCachedStack> REPAIR_WITH_COMBINATION = new AnvilFunction<>() {
    @Override
    public boolean canApply(
        AnvilBehavior<MetaCachedStack> behavior,
        ViewState<MetaCachedStack> state,
        MetaCachedStack result
    ) {
      MetaCachedStack base = state.getBase();
      Material baseMat = base.getItem().getType();
      return baseMat == state.getAddition().getItem().getType()
          && baseMat.getMaxDurability() > 0
          && base.getMeta() instanceof Damageable damageable
          && damageable.getDamage() > 0;
    }

    @Override
    public AnvilFunctionResult<MetaCachedStack> getResult(
        AnvilBehavior<MetaCachedStack> behavior,
        ViewState<MetaCachedStack> state,
        MetaCachedStack result
    ) {
      if (!(state.getBase().getMeta() instanceof Damageable baseDamageable
          && state.getAddition().getMeta() instanceof Damageable additionDamageable)) {
        return AnvilFunctionResult.empty();
      }

      int missingDurability = baseDamageable.getDamage();

      if (missingDurability < 1) {
        return AnvilFunctionResult.empty();
      }

      int maxDurability = state.getBase().getItem().getType().getMaxDurability();
      // Restore durability remaining in added item.
      int restoredDurability = maxDurability - additionDamageable.getDamage();
      // Add a bonus 12% total tool durability to the repair.
      restoredDurability += (int) (maxDurability * 0.12);

      // Finalize for later use.
      int resultDamage = Math.max(0, missingDurability - restoredDurability);

      return new AnvilFunctionResult<>() {
        @Override
        public int getLevelCostIncrease() {
          return 2;
        }

        @Override
        public void modifyResult(MetaCachedStack stack) {
          if (stack.getMeta() instanceof Damageable damageable) {
            damageable.setDamage(resultDamage);
          }
        }
      };
    }
  };
  public static final AnvilFunction<MetaCachedStack> COMBINE_ENCHANTMENTS_JAVA;
  public static final AnvilFunction<MetaCachedStack> COMBINE_ENCHANTMENTS_BEDROCK;

  public static final MetaAnvilFunctions INSTANCE = new MetaAnvilFunctions();

  static {
    MetaEnchantmentAccess access = new MetaEnchantmentAccess();
    COMBINE_ENCHANTMENTS_JAVA = new CombineEnchants<>(CombineEnchants.Platform.JAVA, access);
    COMBINE_ENCHANTMENTS_BEDROCK = new CombineEnchants<>(CombineEnchants.Platform.BEDROCK, access);
  }

  private MetaAnvilFunctions() {}

  @Override
  public AnvilFunction<MetaCachedStack> addPriorWorkLevelCost() {
    return PRIOR_WORK_LEVEL_COST;
  }

  @Override
  public AnvilFunction<MetaCachedStack> rename() {
    return RENAME;
  }

  @Override
  public AnvilFunction<MetaCachedStack> setItemPriorWork() {
    return UPDATE_PRIOR_WORK_COST;
  }

  @Override
  public AnvilFunction<MetaCachedStack> repairWithMaterial() {
    return REPAIR_WITH_MATERIAL;
  }

  @Override
  public AnvilFunction<MetaCachedStack> repairWithCombine() {
    return REPAIR_WITH_COMBINATION;
  }

  @Override
  public AnvilFunction<MetaCachedStack> combineEnchantsJava() {
    return COMBINE_ENCHANTMENTS_JAVA;
  }

  @Override
  public AnvilFunction<MetaCachedStack> combineEnchantsBedrock() {
    return COMBINE_ENCHANTMENTS_BEDROCK;
  }

  private static int getRepairCost(@Nullable ItemMeta itemMeta) {
    if (itemMeta instanceof Repairable repairable) {
      return repairable.getRepairCost();
    }
    return 0;
  }

}
