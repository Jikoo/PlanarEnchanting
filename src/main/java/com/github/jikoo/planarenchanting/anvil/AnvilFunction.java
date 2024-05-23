package com.github.jikoo.planarenchanting.anvil;

import com.github.jikoo.planarenchanting.enchant.EnchantData;
import com.github.jikoo.planarenchanting.util.ItemUtil;
import com.github.jikoo.planarenchanting.util.MetaCachedStack;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An interface representing a portion of the functionality of an anvil. By using several in
 * conjunction, it is possible to mimic vanilla behavior very closely.
 */
public interface AnvilFunction {

  /** Constant for adding the level cost from prior work. */
  AnvilFunction PRIOR_WORK_LEVEL_COST = new AnvilFunction() {
    @Override
    public boolean canApply(@NotNull AnvilOperation operation, @NotNull AnvilOperationState state) {
      return true;
    }

    @Override
    public @NotNull AnvilFunctionResult getResult(
        @NotNull AnvilOperation operation,
        @NotNull AnvilOperationState state) {
      return new AnvilFunctionResult() {
        @Override
        public int getLevelCostIncrease() {
          return ItemUtil.getRepairCost(state.getBase().getMeta())
              + ItemUtil.getRepairCost(state.getAddition().getMeta());
        }
      };
    }
  };

  /** Constant for adding the level cost for a renaming operation. */
  AnvilFunction RENAME = new AnvilFunction() {
    @Override
    public boolean canApply(@NotNull AnvilOperation operation, @NotNull AnvilOperationState state) {
      var itemMeta = state.getBase().getMeta();

      // If names are not the same, can be applied.
      return itemMeta != null
          && !Objects.equals(itemMeta.getDisplayName(), state.getAnvil().getRenameText());
    }

    @Override
    public @NotNull AnvilFunctionResult getResult(
        @NotNull AnvilOperation operation,
        @NotNull AnvilOperationState state) {
      return new AnvilFunctionResult() {
        @Override
        public int getLevelCostIncrease() {
          // Renames always apply a level cost of 1.
          return 1;
        }

        @Override
        public void modifyResult(@Nullable ItemMeta itemMeta) {
          if (itemMeta == null) {
            return;
          }

          itemMeta.setDisplayName(state.getAnvil().getRenameText());
          if (itemMeta instanceof Repairable repairable) {
            int repairCost = Math.max(
                ItemUtil.getRepairCost(state.getBase().getMeta()),
                ItemUtil.getRepairCost(state.getAddition().getMeta()));
            repairable.setRepairCost(repairCost);
          }
        }
      };
    }
  };

  /** Constant for updating prior work to new value. */
  AnvilFunction UPDATE_PRIOR_WORK_COST = new AnvilFunction() {
    @Override
    public boolean canApply(@NotNull AnvilOperation operation, @NotNull AnvilOperationState state) {
      return state.getBase().getMeta() instanceof Repairable;
    }

    @Override
    public @NotNull AnvilFunctionResult getResult(
        @NotNull AnvilOperation operation,
        @NotNull AnvilOperationState state) {

      return new AnvilFunctionResult() {
        @Override
        public int getLevelCostIncrease() {
          return 0;
        }

        @Override
        public void modifyResult(@Nullable ItemMeta itemMeta) {
          if (itemMeta instanceof Repairable repairable) {
            int priorRepairCost = Math.max(
                ItemUtil.getRepairCost(state.getBase().getMeta()),
                ItemUtil.getRepairCost(state.getAddition().getMeta()));
            repairable.setRepairCost(priorRepairCost * 2 + 1);
          }
        }
      };
    }
  };

  /** Constant for using materials to restore durability to the base item. */
  AnvilFunction REPAIR_WITH_MATERIAL = new AnvilFunction() {
    @Override
    public boolean canApply(@NotNull AnvilOperation operation, @NotNull AnvilOperationState state) {
      MetaCachedStack base = state.getBase();
      return operation.itemRepairedBy(base.getItem(), state.getAddition().getItem())
          && base.getItem().getType().getMaxDurability() > 0
          && base.getMeta() instanceof Damageable damageable
          && damageable.getDamage() > 0;
    }

    @Override
    public @NotNull AnvilFunctionResult getResult(
        @NotNull AnvilOperation operation,
        @NotNull AnvilOperationState state) {
      if (!(state.getBase().getMeta() instanceof Damageable damageable)) {
        // If result is not damageable, it cannot be repaired.
        return AnvilFunctionResult.EMPTY;
      }

      int missingDurability = damageable.getDamage();

      if (missingDurability < 1) {
        // If result is not damaged, no repair.
        return AnvilFunctionResult.EMPTY;
      }

      int repairs = 0;
      // Each repair removes up to 1/4 max durability in damage.
      int damageRepairedPerMaterial = state.getBase().getItem().getType().getMaxDurability() / 4;

      while (missingDurability > 0 && repairs < state.getAddition().getItem().getAmount()) {
        missingDurability -= damageRepairedPerMaterial;
        ++repairs;
      }

      // Finalize for later use.
      int totalRepairs = repairs;
      int resultDamage = Math.max(0, missingDurability);

      return new AnvilFunctionResult() {
        @Override
        public int getLevelCostIncrease() {
          return totalRepairs;
        }

        @Override
        public int getMaterialCostIncrease() {
          return totalRepairs;
        }

        @Override
        public void modifyResult(@Nullable ItemMeta itemMeta) {
          if (itemMeta instanceof Damageable damageable) {
            damageable.setDamage(resultDamage);
          }
        }
      };
    }
  };

  /** Constant for using identical materials to restore durability. */
  AnvilFunction REPAIR_WITH_COMBINATION = new AnvilFunction() {
    @Override
    public boolean canApply(@NotNull AnvilOperation operation, @NotNull AnvilOperationState state) {
      Material baseType = state.getBase().getItem().getType();
      return baseType == state.getAddition().getItem().getType()
          && baseType.getMaxDurability() > 0
          && state.getBase().getMeta() instanceof Damageable damageable
          && damageable.getDamage() > 0;
    }

    @Override
    public @NotNull AnvilFunctionResult getResult(
        @NotNull AnvilOperation operation,
        @NotNull AnvilOperationState state) {
      if (!(state.getBase().getMeta() instanceof Damageable baseDamageable
          && state.getAddition().getMeta() instanceof Damageable additionDamageable)) {
        return AnvilFunctionResult.EMPTY;
      }

      int missingDurability = baseDamageable.getDamage();

      if (missingDurability < 1) {
        return AnvilFunctionResult.EMPTY;
      }

      int maxDurability = state.getBase().getItem().getType().getMaxDurability();
      // Restore durability remaining in added item.
      int restoredDurability = maxDurability - additionDamageable.getDamage();
      // Add a bonus 12% total tool durability to the repair.
      restoredDurability += maxDurability * .12;

      // Finalize for later use.
      int resultDamage = Math.max(0, missingDurability - restoredDurability);

      return new AnvilFunctionResult() {
        @Override
        public int getLevelCostIncrease() {
          return 2;
        }

        @Override
        public void modifyResult(@Nullable ItemMeta itemMeta) {
          if (itemMeta instanceof Damageable damageable) {
            damageable.setDamage(resultDamage);
          }
        }
      };
    }
  };

  /** Constant for combining enchantments from source and target like Java Edition. */
  AnvilFunction COMBINE_ENCHANTMENTS_JAVA_EDITION = new CombineEnchantments() {
    @Override
    protected int getTotalCost(int baseCost, int oldLevel, int newLevel) {
      return baseCost * newLevel;
    }

    @Override
    protected int getNonApplicableCost() {
      return 1;
    }
  };

  /** Constant for combining enchantments from source and target like Bedrock Edition. */
  AnvilFunction COMBINE_ENCHANTMENTS_BEDROCK_EDITION = new CombineEnchantments() {
    @Override
    protected int getAnvilCost(Enchantment enchantment, boolean isFromBook) {
      EnchantData enchantData = EnchantData.of(enchantment);
      if (!enchantData.getSecondaryItems().isTagged(Material.TRIDENT)) {
        return super.getAnvilCost(enchantment, isFromBook);
      }

      int base = enchantData.getAnvilCost();

      // Bedrock Edition rarity is 1 tier lower for trident enchantments.
      return Math.max(1, isFromBook ? base / 4 : base / 2);
    }

    @Override
    protected int getTotalCost(int baseCost, int oldLevel, int newLevel) {
      if (oldLevel >= newLevel) {
        return 0;
      }

      return baseCost * (newLevel - oldLevel);
    }

    @Override
    protected int getNonApplicableCost() {
      return 0;
    }
  };

  /**
   * Check if the function is capable of generating a usable result. Note that this may be a quick
   * cursory check - the function may yield an empty result even if it initially reported itself
   * applicable. The only guarantee this method makes is that retrieving and using a result will not
   * cause an error if its return value is respected.
   *
   * @param operation the {@link AnvilOperation} being performed
   * @param state the {@link AnvilOperationState} the state of the {@code AnvilOperation} in use
   * @return whether the {@link AnvilFunction} can generate an {@link AnvilFunctionResult}
   */
  boolean canApply(@NotNull AnvilOperation operation, @NotNull AnvilOperationState state);

  /**
   * Get an {@link AnvilFunctionResult} used to apply the changes from the function based on the
   * provided anvil operation state and settings.
   *
   * @param operation the {@link AnvilOperation} being performed
   * @param state the {@link AnvilOperationState} the state of the anvil in use
   * @return the resulting applicable changes
   */
  @NotNull AnvilFunctionResult getResult(
      @NotNull AnvilOperation operation,
      @NotNull AnvilOperationState state);

}
