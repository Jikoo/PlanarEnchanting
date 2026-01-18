package com.github.jikoo.planarenchanting.anvil;

import com.github.jikoo.planarenchanting.util.ItemUtil;
import com.github.jikoo.planarenchanting.util.MetaCachedStack;
import io.papermc.paper.registry.keys.ItemTypeKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum AnvilFunctions {
  ; // Empty enum to hold constants.

  /** Constant for adding the level cost from prior work. */
  public static final AnvilFunction PRIOR_WORK_LEVEL_COST = new AnvilFunction() {
    @Override
    public boolean canApply(@NotNull AnvilBehavior behavior, @NotNull AnvilState state) {
      return true;
    }

    @Override
    public @NotNull AnvilFunctionResult getResult(
        @NotNull AnvilBehavior behavior,
        @NotNull AnvilState state) {
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
  public static final AnvilFunction RENAME = new AnvilFunction() {
    @Override
    public boolean canApply(@NotNull AnvilBehavior behavior, @NotNull AnvilState state) {
      var itemMeta = state.getBase().getMeta();

      if (itemMeta == null) {
        return false;
      }

      // If names are not the same, can be applied.
      Component customName = itemMeta.customName();
      String anvilText = state.getAnvil().getRenameText();
      if (customName == null) {
        return anvilText != null;
      }
      return !LegacyComponentSerializer.legacySection().serialize(customName).equals(anvilText);
    }

    @Override
    public @NotNull AnvilFunctionResult getResult(
        @NotNull AnvilBehavior behavior,
        @NotNull AnvilState state) {
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

          String anvilText = state.getAnvil().getRenameText();
          itemMeta.customName(anvilText == null ? null : Component.text(anvilText));
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
  public static final AnvilFunction UPDATE_PRIOR_WORK_COST = new AnvilFunction() {
    @Override
    public boolean canApply(@NotNull AnvilBehavior behavior, @NotNull AnvilState state) {
      return state.getBase().getMeta() instanceof Repairable;
    }

    @Override
    public @NotNull AnvilFunctionResult getResult(
        @NotNull AnvilBehavior behavior,
        @NotNull AnvilState state) {

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
  public static final AnvilFunction REPAIR_WITH_MATERIAL = new AnvilFunction() {
    @Override
    public boolean canApply(@NotNull AnvilBehavior behavior, @NotNull AnvilState state) {
      MetaCachedStack base = state.getBase();
      return behavior.itemRepairedBy(base, state.getAddition())
          && base.getItem().getType().getMaxDurability() > 0
          && base.getMeta() instanceof Damageable damageable
          && damageable.getDamage() > 0;
    }

    @Override
    public @NotNull AnvilFunctionResult getResult(
        @NotNull AnvilBehavior behavior,
        @NotNull AnvilState state) {
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
  public static final AnvilFunction REPAIR_WITH_COMBINATION = new AnvilFunction() {
    @Override
    public boolean canApply(@NotNull AnvilBehavior behavior, @NotNull AnvilState state) {
      Material baseType = state.getBase().getItem().getType();
      return baseType == state.getAddition().getItem().getType()
          && baseType.getMaxDurability() > 0
          && state.getBase().getMeta() instanceof Damageable damageable
          && damageable.getDamage() > 0;
    }

    @Override
    public @NotNull AnvilFunctionResult getResult(
        @NotNull AnvilBehavior behavior,
        @NotNull AnvilState state) {
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
  public static final AnvilFunction COMBINE_ENCHANTMENTS_JAVA_EDITION = new CombineEnchantments() {
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
  public static final AnvilFunction COMBINE_ENCHANTMENTS_BEDROCK_EDITION = new CombineEnchantments() {
    @Override
    protected int getAnvilCost(Enchantment enchantment, boolean isFromBook) {
      if (!enchantment.getSupportedItems().contains(ItemTypeKeys.TRIDENT)) {
        return super.getAnvilCost(enchantment, isFromBook);
      }

      int base = enchantment.getAnvilCost();

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

}
