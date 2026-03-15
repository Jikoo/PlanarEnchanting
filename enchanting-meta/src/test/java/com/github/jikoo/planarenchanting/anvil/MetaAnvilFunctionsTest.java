package com.github.jikoo.planarenchanting.anvil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.view.AnvilView;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Default basic AnvilFunctions")
@TestInstance(Lifecycle.PER_CLASS)
class MetaAnvilFunctionsTest {

  @Nested
  class PriorWorkLevelCost {

    private final AnvilFunction<MetaCachedStack> function =  MetaAnvilFunctions.PRIOR_WORK_LEVEL_COST;

    @Test
    void canApply() {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      assertThat(
          "Prior work level cost always applies",
          function.canApply(behavior, state, resultStack),
          is(true)
      );
    }

    @ParameterizedTest
    @MethodSource("getPriorWork")
    void getResult(int baseWork, int addedWork) {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      RepairableMeta meta = mock();
      doReturn(baseWork).when(meta).getRepairCost();
      MetaCachedStack metaStack = mock();
      doReturn(meta).when(metaStack).getMeta();
      doReturn(metaStack).when(state).getBase();

      meta = mock();
      doReturn(addedWork).when(meta).getRepairCost();
      metaStack = mock();
      doReturn(meta).when(metaStack).getMeta();
      doReturn(metaStack).when(state).getAddition();

      var result = function.getResult(behavior, state, resultStack);
      assertThat(
          "Cost must be total prior work",
          result.getLevelCostIncrease(),
          is(baseWork + addedWork)
      );
      assertThat("Material cost is unchanged", result.getMaterialCostIncrease(), is(0));

      resultStack = mock();
      result.modifyResult(resultStack);
      verifyNoInteractions(resultStack);
    }

    @Test
    void getResult() {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      ItemMeta meta = mock();
      MetaCachedStack metaStack = mock();
      doReturn(meta).when(metaStack).getMeta();
      doReturn(metaStack).when(state).getBase();

      meta = mock();
      metaStack = mock();
      doReturn(meta).when(metaStack).getMeta();
      doReturn(metaStack).when(state).getAddition();

      var result = function.getResult(behavior, state, resultStack);
      assertThat(
          "Prior work must fall through to 0",
          result.getLevelCostIncrease(),
          is(0)
      );
      assertThat("Material cost is unchanged", result.getMaterialCostIncrease(), is(0));

      resultStack = mock();
      result.modifyResult(resultStack);
      verifyNoInteractions(resultStack);
    }

    private static @NonNull Collection<Arguments> getPriorWork() {
      Collection<Arguments> arguments = new ArrayList<>();
      int [] values = { 0, 1, 3, 7, 15, 31 };

      for (int base : values) {
        for (int added : values) {
          arguments.add(Arguments.of(base, added));
        }
      }

      return arguments;
    }

  }

  @Nested
  class Rename {

    private final AnvilFunction<MetaCachedStack> function = MetaAnvilFunctions.RENAME;

    @DisplayName("Rename requires ItemMeta")
    @Test
    void canApplyRequiresMeta() {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      MetaCachedStack metaStack = mock();
      doReturn(metaStack).when(state).getBase();

      assertThat(
          "Rename requires meta",
          function.canApply(behavior, state, resultStack),
          is(false)
      );
    }

    @DisplayName("Rename requires different name")
    @ParameterizedTest
    @MethodSource("renameSituations")
    void canApply(String anvilName, String baseName, boolean canApply) {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      ItemMeta meta = mock();
      doReturn(baseName != null).when(meta).hasDisplayName();
      doReturn(baseName).when(meta).getDisplayName();
      MetaCachedStack metaStack = mock();
      doReturn(meta).when(metaStack).getMeta();
      doReturn(metaStack).when(state).getBase();

      AnvilView view = mock();
      doReturn(anvilName).when(view).getRenameText();
      doReturn(view).when(state).getOriginalView();

      assertThat("Rename requires different name", function.canApply(behavior, state, resultStack), is(canApply));
    }

    @Test
    void getResultNoMeta() {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      AnvilView view = mock();
      doReturn("sample text").when(view).getRenameText();
      doReturn(view).when(state).getOriginalView();

      AnvilFunctionResult<MetaCachedStack> result = function.getResult(behavior, state, resultStack);

      assertThat("Level cost increase is 1", result.getLevelCostIncrease(), is(1));
      assertThat("Material cost is unchanged", result.getMaterialCostIncrease(), is(0));

      MetaCachedStack stack = mock();
      result.modifyResult(stack);
      verify(stack).getMeta();
      verifyNoMoreInteractions(stack);
    }

    @Test
    void getResultNotRepairable() {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      AnvilView view = mock();
      doReturn("sample text").when(view).getRenameText();
      doReturn(view).when(state).getOriginalView();

      AnvilFunctionResult<MetaCachedStack> result = function.getResult(behavior, state, resultStack);

      assertThat("Level cost increase is 1", result.getLevelCostIncrease(), is(1));
      assertThat("Material cost is unchanged", result.getMaterialCostIncrease(), is(0));

      ItemMeta meta = mock();
      MetaCachedStack stack = mock();
      doReturn(meta).when(stack).getMeta();

      result.modifyResult(stack);

      verify(stack).getMeta();
      verifyNoMoreInteractions(stack);

      verify(meta).setDisplayName(any());
      verifyNoMoreInteractions(meta);
    }

    @Test
    void getResult() {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      AnvilView view = mock();
      doReturn("sample text").when(view).getRenameText();
      doReturn(view).when(state).getOriginalView();
      MetaCachedStack metaStack = mock();
      doReturn(metaStack).when(state).getBase();
      doReturn(metaStack).when(state).getAddition();

      AnvilFunctionResult<MetaCachedStack> result = function.getResult(behavior, state, resultStack);

      assertThat("Level cost increase is 1", result.getLevelCostIncrease(), is(1));
      assertThat("Material cost is unchanged", result.getMaterialCostIncrease(), is(0));

      RepairableMeta meta = mock();
      metaStack = mock();
      doReturn(meta).when(metaStack).getMeta();

      result.modifyResult(metaStack);

      verify(metaStack).getMeta();
      verifyNoMoreInteractions(metaStack);

      verify(meta).setDisplayName(any());
      verify(meta).setRepairCost(anyInt());
      verifyNoMoreInteractions(meta);
    }

    private static Stream<Arguments> renameSituations() {
      String name1 = "Sample text";
      String name2 = "Example text";
      return Stream.of(
          // NON-APPLICABLE
          // Both unnamed
          Arguments.of(null, null, false),
          // Both identically named
          Arguments.of(name1, name1, false),

          // APPLICABLE
          // Only anvil named
          Arguments.of(name1, null, true),
          // Only item named
          Arguments.of(null, name1, true),
          // Both named differently
          Arguments.of(name1, name2, true)
      );
    }

  }

  @Nested
  class UpdatePriorWorkCost {

    private final AnvilFunction<MetaCachedStack> function =  MetaAnvilFunctions.UPDATE_PRIOR_WORK_COST;

    @Test
    void canApplyRequiresRepairable() {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      MetaCachedStack metaStack = mock();
      doReturn(metaStack).when(state).getBase();

      assertThat(
          "Prior work update requires repairable",
          function.canApply(behavior, state, resultStack),
          is(false)
      );
    }

    @Test
    void canApply() {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      RepairableMeta meta = mock();
      MetaCachedStack stack = mock();
      doReturn(meta).when(stack).getMeta();
      doReturn(stack).when(state).getBase();

      assertThat(
          "Prior work update applies",
          function.canApply(behavior, state, resultStack),
          is(true)
      );
    }

    @Test
    void getResultNotRepairable() {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      AnvilFunctionResult<MetaCachedStack> result = function.getResult(behavior, state, resultStack);

      assertThat("Result is not empty", result, is(not(AnvilFunctionResult.empty())));
      assertThat("Level cost must be unchanged", result.getLevelCostIncrease(), is(0));
      assertThat("Material cost must be unchanged", result.getMaterialCostIncrease(), is(0));

      MetaCachedStack stack = mock();
      result.modifyResult(stack);
      verify(stack).getMeta();
      verifyNoMoreInteractions(stack);
    }

    @ParameterizedTest
    @MethodSource("getPriorWork")
    void getResult(int baseCost, int additionCost) {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      RepairableMeta meta = mock();
      doReturn(baseCost).when(meta).getRepairCost();
      MetaCachedStack stack = mock();
      doReturn(meta).when(stack).getMeta();
      doReturn(stack).when(state).getBase();
      meta = mock();
      doReturn(additionCost).when(meta).getRepairCost();
      stack = mock();
      doReturn(meta).when(stack).getMeta();
      doReturn(stack).when(state).getAddition();

      AnvilFunctionResult<MetaCachedStack> result = function.getResult(behavior, state, resultStack);

      assertThat("Result is not empty", result, is(not(AnvilFunctionResult.empty())));
      assertThat("Level cost must be unchanged", result.getLevelCostIncrease(), is(0));
      assertThat("Material cost must be unchanged", result.getMaterialCostIncrease(), is(0));

      meta = mock();
      stack = mock();
      doReturn(meta).when(stack).getMeta();
      result.modifyResult(stack);
      verify(stack).getMeta();
      verify(meta).setRepairCost(anyInt());
    }

    private static @NonNull Collection<Arguments> getPriorWork() {
      Collection<Arguments> arguments = new ArrayList<>();
      int [] values = { 0, 1, 3, 7, 15, 31 };

      for (int base : values) {
        for (int added : values) {
          arguments.add(Arguments.of(base, added));
        }
      }

      return arguments;
    }

  }

  @Nested
  class RepairWithMaterial {

    private final AnvilFunction<MetaCachedStack> function = MetaAnvilFunctions.REPAIR_WITH_MATERIAL;

    @Test
    void canApplyNotRepairedBy() {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      assertThat("Must be repairable by item", function.canApply(behavior, state, resultStack), is(false));
    }

    @Test
    void canApplyNotDurable() {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      doReturn(true).when(behavior).itemRepairedBy(any(), any());

      Material material = mock();
      ItemStack stack = mock();
      doReturn(material).when(stack).getType();
      MetaCachedStack metaStack = mock();
      doReturn(stack).when(metaStack).getItem();
      doReturn(metaStack).when(state).getBase();

      assertThat("Must have durability", function.canApply(behavior, state, resultStack), is(false));
    }

    @Test
    void canApplyNotDamageable() {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      doReturn(true).when(behavior).itemRepairedBy(any(), any());

      Material material = mock();
      doReturn((short) 100).when(material).getMaxDurability();
      ItemStack stack = mock();
      doReturn(material).when(stack).getType();
      MetaCachedStack metaStack = mock();
      doReturn(stack).when(metaStack).getItem();
      doReturn(metaStack).when(state).getBase();

      assertThat("Must be damageable", function.canApply(behavior, state, resultStack), is(false));
    }

    @Test
    void canApplyNotDamaged() {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      doReturn(true).when(behavior).itemRepairedBy(any(), any());

      Material material = mock();
      doReturn((short) 100).when(material).getMaxDurability();
      ItemStack stack = mock();
      doReturn(material).when(stack).getType();
      DamageableMeta meta = mock();
      MetaCachedStack metaStack = mock();
      doReturn(stack).when(metaStack).getItem();
      doReturn(meta).when(metaStack).getMeta();
      doReturn(metaStack).when(state).getBase();

      assertThat("Must have damage", function.canApply(behavior, state, resultStack), is(false));
    }

    @Test
    void canApply() {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      doReturn(true).when(behavior).itemRepairedBy(any(), any());

      Material material = mock();
      doReturn((short) 4).when(material).getMaxDurability();
      ItemStack stack = mock();
      doReturn(material).when(stack).getType();
      DamageableMeta meta = mock();
      doReturn(4).when(meta).getDamage();
      MetaCachedStack metaStack = mock();
      doReturn(stack).when(metaStack).getItem();
      doReturn(meta).when(metaStack).getMeta();
      doReturn(metaStack).when(state).getBase();

      assertThat("Base can be repaired", function.canApply(behavior, state, resultStack), is(true));
    }

    @Test
    void getResultNotDamageable() {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      MetaCachedStack metaStack = mock();
      doReturn(metaStack).when(state).getBase();

      assertThat(
          "Undamageable base yields empty result",
          function.getResult(behavior, state, resultStack),
          is(AnvilFunctionResult.empty())
      );
    }

    @Test
    void getResultNotDamaged() {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      DamageableMeta meta = mock();
      MetaCachedStack metaStack = mock();
      doReturn(meta).when(metaStack).getMeta();
      doReturn(metaStack).when(state).getBase();

      assertThat(
          "Undamaged base yields empty result",
          function.getResult(behavior, state, resultStack),
          is(AnvilFunctionResult.empty())
      );
    }

    @ParameterizedTest
    @CsvSource({"1,1,3", "64,4,0"})
    void getResult(
        int additionAmount,
        int expectedRepairs,
        int expectedDamage
    ) {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      Material material = mock();
      doReturn((short) 4).when(material).getMaxDurability();
      ItemStack stack = mock();
      doReturn(material).when(stack).getType();
      DamageableMeta meta = mock();
      doReturn(4).when(meta).getDamage();
      MetaCachedStack metaStack = mock();
      doReturn(stack).when(metaStack).getItem();
      doReturn(meta).when(metaStack).getMeta();
      doReturn(metaStack).when(state).getBase();

      stack = mock();
      doReturn(additionAmount).when(stack).getAmount();
      metaStack = mock();
      doReturn(stack).when(metaStack).getItem();
      doReturn(metaStack).when(state).getAddition();

      AnvilFunctionResult<MetaCachedStack> result = function.getResult(behavior, state, resultStack);
      assertThat("Cost is repair count", result.getLevelCostIncrease(), is(expectedRepairs));
      assertThat("Cost is repair count", result.getMaterialCostIncrease(), is(expectedRepairs));

      final MetaCachedStack notDamageable = mock();
      assertDoesNotThrow(() -> result.modifyResult(notDamageable));

      meta = mock();
      metaStack = mock();
      doReturn(meta).when(metaStack).getMeta();

      result.modifyResult(metaStack);
      verify(meta).setDamage(expectedDamage);
    }

  }

  @Nested
  class RepairWithCombination {

    private final AnvilFunction<MetaCachedStack> function = MetaAnvilFunctions.REPAIR_WITH_COMBINATION;

    @Test
    void canApplyNotSameType() {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      doReturn(true).when(behavior).itemRepairedBy(any(), any());

      // Base
      Material material = mock();
      ItemStack stack = mock();
      doReturn(material).when(stack).getType();
      MetaCachedStack metaStack = mock();
      doReturn(stack).when(metaStack).getItem();
      doReturn(metaStack).when(state).getBase();

      // Addition
      stack = mock();
      metaStack = mock();
      doReturn(stack).when(metaStack).getItem();
      doReturn(metaStack).when(state).getAddition();

      assertThat("Non-like items cannot combine", function.canApply(behavior, state, resultStack), is(false));
    }

    @Test
    void canApplyNotDurable() {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      doReturn(true).when(behavior).itemRepairedBy(any(), any());

      // Base
      Material material = mock();
      ItemStack stack = mock();
      doReturn(material).when(stack).getType();
      MetaCachedStack metaStack = mock();
      doReturn(stack).when(metaStack).getItem();
      doReturn(metaStack).when(state).getBase();

      // Addition
      stack = mock();
      doReturn(material).when(stack).getType();
      metaStack = mock();
      doReturn(stack).when(metaStack).getItem();
      doReturn(metaStack).when(state).getAddition();

      assertThat("Item without durability cannot combine", function.canApply(behavior, state, resultStack), is(false));
    }

    @Test
    void canApplyNotDamageable() {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      doReturn(true).when(behavior).itemRepairedBy(any(), any());

      // Base
      Material material = mock();
      doReturn((short) 4).when(material).getMaxDurability();
      ItemStack stack = mock();
      doReturn(material).when(stack).getType();
      MetaCachedStack metaStack = mock();
      doReturn(stack).when(metaStack).getItem();
      doReturn(metaStack).when(state).getBase();

      // Addition
      stack = mock();
      doReturn(material).when(stack).getType();
      metaStack = mock();
      doReturn(stack).when(metaStack).getItem();
      doReturn(metaStack).when(state).getAddition();

      assertThat("Item must be Damageable", function.canApply(behavior, state, resultStack), is(false));
    }

    @Test
    void canApplyNotDamaged() {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      doReturn(true).when(behavior).itemRepairedBy(any(), any());

      // Base
      Material material = mock();
      doReturn((short) 4).when(material).getMaxDurability();
      ItemStack stack = mock();
      doReturn(material).when(stack).getType();
      DamageableMeta meta = mock();
      MetaCachedStack metaStack = mock();
      doReturn(stack).when(metaStack).getItem();
      doReturn(meta).when(metaStack).getMeta();
      doReturn(metaStack).when(state).getBase();

      // Addition
      stack = mock();
      doReturn(material).when(stack).getType();
      metaStack = mock();
      doReturn(stack).when(metaStack).getItem();
      doReturn(metaStack).when(state).getAddition();

      assertThat("Item must be damaged", function.canApply(behavior, state, resultStack), is(false));
    }

    @Test
    void canApply() {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      doReturn(true).when(behavior).itemRepairedBy(any(), any());

      // Base
      Material material = mock();
      doReturn((short) 4).when(material).getMaxDurability();
      ItemStack stack = mock();
      doReturn(material).when(stack).getType();
      DamageableMeta meta = mock();
      doReturn(4).when(meta).getDamage();
      MetaCachedStack metaStack = mock();
      doReturn(stack).when(metaStack).getItem();
      doReturn(meta).when(metaStack).getMeta();
      doReturn(metaStack).when(state).getBase();

      // Addition
      stack = mock();
      doReturn(material).when(stack).getType();
      metaStack = mock();
      doReturn(stack).when(metaStack).getItem();
      doReturn(metaStack).when(state).getAddition();

      assertThat("Items can be combined", function.canApply(behavior, state, resultStack), is(true));
    }


    @Test
    void getResultBaseNotRepairable() {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      // Base
      ItemStack stack = mock();
      MetaCachedStack metaStack = mock();
      doReturn(stack).when(metaStack).getItem();
      doReturn(metaStack).when(state).getBase();

      // Addition
      stack = mock();
      metaStack = mock();
      doReturn(stack).when(metaStack).getItem();
      doReturn(metaStack).when(state).getAddition();

      assertThat(
          "Undamaged base yields empty result",
          function.getResult(behavior, state, resultStack),
          is(AnvilFunctionResult.empty())
      );
    }

    @Test
    void getResultAdditionNotRepairable() {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      // Base
      ItemStack stack = mock();
      DamageableMeta meta = mock();
      MetaCachedStack metaStack = mock();
      doReturn(stack).when(metaStack).getItem();
      doReturn(meta).when(metaStack).getMeta();
      doReturn(metaStack).when(state).getBase();

      // Addition
      stack = mock();
      metaStack = mock();
      doReturn(stack).when(metaStack).getItem();
      doReturn(metaStack).when(state).getAddition();

      assertThat(
          "Undamaged base yields empty result",
          function.getResult(behavior, state, resultStack),
          is(AnvilFunctionResult.empty())
      );
    }

    @Test
    void getResultNoDamage() {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      // Base
      ItemStack stack = mock();
      DamageableMeta meta = mock();
      MetaCachedStack metaStack = mock();
      doReturn(stack).when(metaStack).getItem();
      doReturn(meta).when(metaStack).getMeta();
      doReturn(metaStack).when(state).getBase();

      // Addition
      stack = mock();
      meta = mock();
      metaStack = mock();
      doReturn(stack).when(metaStack).getItem();
      doReturn(meta).when(metaStack).getMeta();
      doReturn(metaStack).when(state).getAddition();

      assertThat(
          "Undamaged base yields empty result",
          function.getResult(behavior, state, resultStack),
          is(AnvilFunctionResult.empty())
      );
    }

    @Test
    void getResult() {
      AnvilBehavior<MetaCachedStack> behavior = mock();
      ViewState<MetaCachedStack> state = mock();
      MetaCachedStack resultStack = mock();

      Material material = mock();
      doReturn((short) 100).when(material).getMaxDurability();

      // Base
      ItemStack stack = mock();
      doReturn(material).when(stack).getType();
      DamageableMeta meta = mock();
      doReturn(100).when(meta).getDamage();
      MetaCachedStack metaStack = mock();
      doReturn(stack).when(metaStack).getItem();
      doReturn(meta).when(metaStack).getMeta();
      doReturn(metaStack).when(state).getBase();

      // Addition
      stack = mock();
      doReturn(material).when(stack).getType();
      meta = mock();
      doReturn(100).when(meta).getDamage();
      metaStack = mock();
      doReturn(stack).when(metaStack).getItem();
      doReturn(meta).when(metaStack).getMeta();
      doReturn(metaStack).when(state).getAddition();

      AnvilFunctionResult<MetaCachedStack> result = function.getResult(behavior, state, resultStack);
      assertThat("Combine repair costs 2", result.getLevelCostIncrease(), is(2));
      assertThat("Material cost is unchanged", result.getMaterialCostIncrease(), is(0));

      final MetaCachedStack notDamageable = mock();
      assertDoesNotThrow(() -> result.modifyResult(notDamageable));

      meta = mock();
      metaStack = mock();
      doReturn(meta).when(metaStack).getMeta();

      result.modifyResult(metaStack);
      verify(meta).setDamage(88);
    }

  }

  @Test
  void gettersFetchConstants() {
    // A bit of a silly test, but might prevent accidents.
    MetaAnvilFunctions functions = MetaAnvilFunctions.INSTANCE;
    assertThat(
        "Provided function is constant",
        functions.addPriorWorkLevelCost(),
        is(sameInstance(MetaAnvilFunctions.PRIOR_WORK_LEVEL_COST))
    );
    assertThat(
        "Provided function is constant",
        functions.rename(),
        is(sameInstance(MetaAnvilFunctions.RENAME))
    );
    assertThat(
        "Provided function is constant",
        functions.setItemPriorWork(),
        is(sameInstance(MetaAnvilFunctions.UPDATE_PRIOR_WORK_COST))
    );
    assertThat(
        "Provided function is constant",
        functions.repairWithMaterial(),
        is(sameInstance(MetaAnvilFunctions.REPAIR_WITH_MATERIAL))
    );
    assertThat(
        "Provided function is constant",
        functions.repairWithCombine(),
        is(sameInstance(MetaAnvilFunctions.REPAIR_WITH_COMBINATION))
    );
    assertThat(
        "Provided function is constant",
        functions.combineEnchantsJava(),
        is(sameInstance(MetaAnvilFunctions.COMBINE_ENCHANTMENTS_JAVA))
    );
    assertThat(
        "Provided function is constant",
        functions.combineEnchantsBedrock(),
        is(sameInstance(MetaAnvilFunctions.COMBINE_ENCHANTMENTS_BEDROCK))
    );
  }

  private interface RepairableMeta extends ItemMeta, Repairable {
    @Override
    @NonNull RepairableMeta clone();
  }

  private interface DamageableMeta extends ItemMeta, Damageable {
    @Override
    @NonNull DamageableMeta clone();
  }

}