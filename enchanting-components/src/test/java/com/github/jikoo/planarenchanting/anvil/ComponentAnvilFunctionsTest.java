package com.github.jikoo.planarenchanting.anvil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.github.jikoo.planarenchanting.util.mock.ServerMocks;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
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
class ComponentAnvilFunctionsTest {

  @BeforeAll
  void beforeAll() {
    ServerMocks.mockServer();
    // Touch DataComponentType so it is available during later mocking.
    DataComponentTypes.DAMAGE.key();
  }

  @Nested
  class PriorWorkLevelCost {

    private final AnvilFunction<ItemStack> function =  ComponentAnvilFunctions.PRIOR_WORK_LEVEL_COST;

    @Test
    void canApply() {
      AnvilBehavior<ItemStack> behavior = mock();
      ViewState<ItemStack> state = mock();
      ItemStack resultStack = mock();

      assertThat(
          "Prior work level cost always applies",
          function.canApply(behavior, state, resultStack),
          is(true)
      );
    }

    @ParameterizedTest
    @MethodSource("getPriorWork")
    void getResult(int baseWork, int addedWork) {
      AnvilBehavior<ItemStack> behavior = mock();
      ViewState<ItemStack> state = mock();

      ItemStack stack = mock();
      doReturn(baseWork).when(stack).getData(DataComponentTypes.REPAIR_COST);
      doReturn(stack).when(state).getBase();
      stack = mock();
      doReturn(addedWork).when(stack).getData(DataComponentTypes.REPAIR_COST);
      doReturn(stack).when(state).getAddition();

      var result = function.getResult(behavior, state, mock());
      assertThat(
          "Cost must be total prior work",
          result.getLevelCostIncrease(),
          is(baseWork + addedWork)
      );
      assertThat("Material cost is unchanged", result.getMaterialCostIncrease(), is(0));

      ItemStack resultStack = mock();
      result.modifyResult(resultStack);
      verifyNoInteractions(resultStack);
    }

    private static @NotNull Collection<Arguments> getPriorWork() {
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

    private final AnvilFunction<ItemStack> function = ComponentAnvilFunctions.RENAME;

    @DisplayName("Rename requires different name")
    @ParameterizedTest
    @MethodSource("renameSituations")
    void canApplyRequiresNameChange(String anvilName, Component baseName, boolean canApply) {
      AnvilBehavior<ItemStack> behavior = mock();
      ViewState<ItemStack> state = mock();
      ItemStack resultStack = mock();

      AnvilView view = mock();
      doReturn(anvilName).when(view).getRenameText();
      doReturn(view).when(state).getAnvilView();

      ItemStack stack = mock();
      doReturn(baseName).when(stack).getData(DataComponentTypes.CUSTOM_NAME);
      doReturn(stack).when(state).getBase();

      assertThat("Rename requires different name", function.canApply(behavior, state, resultStack), is(canApply));
    }

    @ParameterizedTest
    @MethodSource("resetName")
    void getResultResetName(String anvilText) {
      AnvilBehavior<ItemStack> behavior = mock();
      ViewState<ItemStack> state = mock();
      ItemStack resultStack = mock();

      AnvilView view = mock();
      doReturn(anvilText).when(view).getRenameText();
      doReturn(view).when(state).getAnvilView();

      ItemStack stack = mock();
      doReturn(stack).when(state).getBase();
      doReturn(stack).when(state).getAddition();

      AnvilFunctionResult<ItemStack> result = function.getResult(behavior, state, resultStack);

      assertThat("Level cost increase is 1", result.getLevelCostIncrease(), is(1));
      assertThat("Material cost is unchanged", result.getMaterialCostIncrease(), is(0));

      stack = mock();
      result.modifyResult(stack);

      verify(stack).resetData(DataComponentTypes.CUSTOM_NAME);
      verify(stack).setData(eq(DataComponentTypes.REPAIR_COST), anyInt());
    }

    @Test
    void getResultSetName() {
      AnvilBehavior<ItemStack> behavior = mock();
      ViewState<ItemStack> state = mock();
      ItemStack resultStack = mock();

      ItemStack stack = mock();
      doReturn(stack).when(state).getBase();
      doReturn(stack).when(state).getAddition();

      AnvilView view = mock();
      doReturn("sample text").when(view).getRenameText();
      doReturn(view).when(state).getAnvilView();

      AnvilFunctionResult<ItemStack> result = function.getResult(behavior, state, resultStack);

      assertThat("Level cost increase is 1", result.getLevelCostIncrease(), is(1));
      assertThat("Material cost is unchanged", result.getMaterialCostIncrease(), is(0));

      stack = mock();
      result.modifyResult(stack);

      verify(stack).setData(eq(DataComponentTypes.CUSTOM_NAME), any(Component.class));
      verify(stack).setData(eq(DataComponentTypes.REPAIR_COST), anyInt());
    }

    private static Stream<Arguments> renameSituations() {
      String name1 = "Sample text";
      String name2 = "Example text";
      return Stream.of(
          // NON-APPLICABLE
          // Both unnamed
          Arguments.of(null, null, false),
          // Both identically named
          Arguments.of(name1, Component.text(name1), false),
          // Anvil text empty
          Arguments.of("", null, false),

          // APPLICABLE
          // Only anvil named
          Arguments.of(name1, null, true),
          // Only item named
          Arguments.of(null, Component.text(name1), true),
          Arguments.of("", Component.text(name1), true),
          // Both named differently
          Arguments.of(name1, Component.text(name2), true)
      );
    }

    private static String[] resetName() {
      return new String[] { null, "" };
    }

  }

  @Nested
  class UpdatePriorWorkCost {

    private final AnvilFunction<ItemStack> function =  ComponentAnvilFunctions.UPDATE_PRIOR_WORK_COST;

    @Test
    void canApply() {
      AnvilBehavior<ItemStack> behavior = mock();
      ViewState<ItemStack> state = mock();
      ItemStack resultStack = mock();

      assertThat(
          "Prior work update always applies",
          function.canApply(behavior, state, resultStack),
          is(true)
      );
    }

    @ParameterizedTest
    @MethodSource("getPriorWork")
    void testPriorWorkUpdate(int baseWork, int addedWork) {
      AnvilBehavior<ItemStack> behavior = mock();
      ViewState<ItemStack> state = mock();
      ItemStack resultStack = mock();

      ItemStack stack = mock();
      doReturn(baseWork).when(stack).getData(DataComponentTypes.REPAIR_COST);
      doReturn(stack).when(state).getBase();
      stack = mock();
      doReturn(addedWork).when(stack).getData(DataComponentTypes.REPAIR_COST);
      doReturn(stack).when(state).getAddition();

      var result = function.getResult(behavior, state, resultStack);
      assertThat("Result is not empty", result, is(not(AnvilFunctionResult.empty())));
      assertThat("Cost must be unchanged", result.getLevelCostIncrease(), is(0));
      assertThat("Material cost is unchanged", result.getMaterialCostIncrease(), is(0));

      stack = mock();

      result.modifyResult(stack);
      verify(stack).setData(eq(DataComponentTypes.REPAIR_COST), anyInt());
    }

    private static @NotNull Collection<Arguments> getPriorWork() {
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

    private final AnvilFunction<ItemStack> function = ComponentAnvilFunctions.REPAIR_WITH_MATERIAL;

    @Test
    void canApplyNoDamage() {
      AnvilBehavior<ItemStack> behavior = mock();
      ViewState<ItemStack> state = mock();
      ItemStack resultStack = mock();
      ItemStack stack = mock();
      doReturn(stack).when(state).getBase();

      assertThat("Cannot apply to undamaged item", function.canApply(behavior, state, resultStack), is(false));
    }

    @Test
    void canApplyNotRepairedBy() {
      AnvilBehavior<ItemStack> behavior = mock();
      ViewState<ItemStack> state = mock();
      ItemStack resultStack = mock();
      ItemStack stack = mock();
      doReturn(1).when(stack).getData(DataComponentTypes.DAMAGE);
      doReturn(stack).when(state).getBase();

      assertThat(
          "Cannot apply with unusable addition",
          function.canApply(behavior, state, resultStack),
          is(false)
      );
    }

    @Test
    void canApply() {
      AnvilBehavior<ItemStack> behavior = mock();
      doReturn(true).when(behavior).itemRepairedBy(any(), any());
      ViewState<ItemStack> state = mock();
      ItemStack resultStack = mock();
      ItemStack stack = mock();
      doReturn(1).when(stack).getData(DataComponentTypes.DAMAGE);
      doReturn(stack).when(state).getBase();

      assertThat(
          "Must apply to damaged and repairable item",
          function.canApply(behavior, state, resultStack),
          is(true)
      );
    }

    @Test
    void getResultNoDamage() {
      AnvilBehavior<ItemStack> behavior = mock();
      ViewState<ItemStack> state = mock();
      ItemStack resultStack = mock();
      ItemStack stack = mock();
      doReturn(stack).when(state).getBase();

      assertThat(
          "Item without damage yields empty result",
          function.getResult(behavior, state, resultStack),
          is(AnvilFunctionResult.empty())
      );
    }

    @Test
    void getResultNoMaxDamage() {
      AnvilBehavior<ItemStack> behavior = mock();
      ViewState<ItemStack> state = mock();
      ItemStack resultStack = mock();
      ItemStack stack = mock();
      doReturn(1).when(stack).getData(DataComponentTypes.DAMAGE);
      doReturn(stack).when(state).getBase();

      assertThat(
          "Item without max damage yields empty result",
          function.getResult(behavior, state, resultStack),
          is(AnvilFunctionResult.empty())
      );
    }

    @ParameterizedTest
    @CsvSource({"1,1,3", "64,4,0"})
    void getResult(int additionAmount, int expectedRepairs, int expectedDamage) {
      AnvilBehavior<ItemStack> behavior = mock();
      ViewState<ItemStack> state = mock();
      ItemStack resultStack = mock();

      ItemStack stack = mock();
      doReturn(4).when(stack).getData(DataComponentTypes.DAMAGE);
      doReturn(4).when(stack).getData(DataComponentTypes.MAX_DAMAGE);
      doReturn(stack).when(state).getBase();
      stack = mock();
      doReturn(additionAmount).when(stack).getAmount();
      doReturn(stack).when(state).getAddition();

      AnvilFunctionResult<ItemStack> result = function.getResult(behavior, state, resultStack);
      assertThat("Cost is repair count", result.getLevelCostIncrease(), is(expectedRepairs));
      assertThat("Cost is repair count", result.getMaterialCostIncrease(), is(expectedRepairs));

      stack = mock();
      result.modifyResult(stack);
      verify(stack).setData(DataComponentTypes.DAMAGE, expectedDamage);
    }

  }

  @Nested
  class RepairWithCombination {

    private final AnvilFunction<ItemStack> function = ComponentAnvilFunctions.REPAIR_WITH_COMBINATION;

    @Test
    void canApplyNotSameType() {
      AnvilBehavior<ItemStack> behavior = mock();
      ViewState<ItemStack> state = mock();
      ItemStack resultStack = mock();
      ItemStack stack = mock();
      doReturn(Material.DIAMOND_AXE).when(stack).getType();
      doReturn(stack).when(state).getBase();
      stack = mock();
      doReturn(Material.DIAMOND_PICKAXE).when(stack).getType();
      doReturn(stack).when(state).getAddition();

      assertThat("Items must be same type", function.canApply(behavior, state, resultStack), is(false));
    }

    @Test
    void canApplyNoBaseDamage() {
      AnvilBehavior<ItemStack> behavior = mock();
      ViewState<ItemStack> state = mock();
      ItemStack resultStack = mock();
      ItemStack stack = mock();
      doReturn(Material.DIAMOND_PICKAXE).when(stack).getType();
      doReturn(stack).when(state).getBase();
      stack = mock();
      doReturn(Material.DIAMOND_PICKAXE).when(stack).getType();
      doReturn(stack).when(state).getAddition();

      assertThat("Base must be damaged", function.canApply(behavior, state, resultStack), is(false));
    }

    @Test
    void canApplyNoAdditionDamage() {
      AnvilBehavior<ItemStack> behavior = mock();
      ViewState<ItemStack> state = mock();
      ItemStack resultStack = mock();
      ItemStack stack = mock();
      doReturn(Material.DIAMOND_PICKAXE).when(stack).getType();
      doReturn(16).when(stack).getData(DataComponentTypes.DAMAGE);
      doReturn(stack).when(state).getBase();
      stack = mock();
      doReturn(Material.DIAMOND_PICKAXE).when(stack).getType();
      doReturn(stack).when(state).getAddition();

      assertThat("Addition must be damaged", function.canApply(behavior, state, resultStack), is(false));
    }

    @Test
    void canApplyNoMaxDamage() {
      AnvilBehavior<ItemStack> behavior = mock();
      ViewState<ItemStack> state = mock();
      ItemStack resultStack = mock();
      ItemStack stack = mock();
      doReturn(Material.DIAMOND_PICKAXE).when(stack).getType();
      doReturn(16).when(stack).getData(DataComponentTypes.DAMAGE);
      doReturn(stack).when(state).getBase();
      stack = mock();
      doReturn(Material.DIAMOND_PICKAXE).when(stack).getType();
      doReturn(16).when(stack).getData(DataComponentTypes.DAMAGE);
      doReturn(stack).when(state).getAddition();

      assertThat("Base must have max damage", function.canApply(behavior, state, resultStack), is(false));
    }

    @Test
    void canApplyMaxDamageMismatch() {
      AnvilBehavior<ItemStack> behavior = mock();
      ViewState<ItemStack> state = mock();
      ItemStack resultStack = mock();
      ItemStack stack = mock();
      doReturn(Material.DIAMOND_PICKAXE).when(stack).getType();
      doReturn(16).when(stack).getData(DataComponentTypes.DAMAGE);
      doReturn(16).when(stack).getData(DataComponentTypes.MAX_DAMAGE);
      doReturn(stack).when(state).getBase();
      stack = mock();
      doReturn(Material.DIAMOND_PICKAXE).when(stack).getType();
      doReturn(16).when(stack).getData(DataComponentTypes.DAMAGE);
      doReturn(1).when(stack).getData(DataComponentTypes.MAX_DAMAGE);
      doReturn(stack).when(state).getAddition();

      assertThat(
          "Base and addition max damage must match",
          function.canApply(behavior, state, resultStack),
          is(false)
      );
    }

    @Test
    void canApply() {
      AnvilBehavior<ItemStack> behavior = mock();
      ViewState<ItemStack> state = mock();
      ItemStack resultStack = mock();
      ItemStack stack = mock();
      doReturn(Material.DIAMOND_PICKAXE).when(stack).getType();
      doReturn(16).when(stack).getData(DataComponentTypes.DAMAGE);
      doReturn(16).when(stack).getData(DataComponentTypes.MAX_DAMAGE);
      doReturn(stack).when(state).getBase();
      stack = mock();
      doReturn(Material.DIAMOND_PICKAXE).when(stack).getType();
      doReturn(16).when(stack).getData(DataComponentTypes.DAMAGE);
      doReturn(16).when(stack).getData(DataComponentTypes.MAX_DAMAGE);
      doReturn(stack).when(state).getAddition();

      assertThat("Function can apply", function.canApply(behavior, state, resultStack), is(true));
    }

    @Test
    void getResultNoDamage() {
      AnvilBehavior<ItemStack> behavior = mock();
      ViewState<ItemStack> state = mock();
      ItemStack resultStack = mock();
      ItemStack stack = mock();
      doReturn(stack).when(state).getBase();

      assertThat(
          "Item without damage yields empty result",
          function.getResult(behavior, state, resultStack),
          is(AnvilFunctionResult.empty())
      );
    }

    @Test
    void getResultNoMaxDamage() {
      AnvilBehavior<ItemStack> behavior = mock();
      ViewState<ItemStack> state = mock();
      ItemStack resultStack = mock();
      ItemStack stack = mock();
      doReturn(1).when(stack).getData(DataComponentTypes.DAMAGE);
      doReturn(stack).when(state).getBase();

      assertThat(
          "Item without max damage yields empty result",
          function.getResult(behavior, state, resultStack),
          is(AnvilFunctionResult.empty())
      );
    }

    @Test
    void getResult() {
      AnvilBehavior<ItemStack> behavior = mock();
      ViewState<ItemStack> state = mock();
      ItemStack resultStack = mock();
      ItemStack stack = mock();
      doReturn(100).when(stack).getData(DataComponentTypes.DAMAGE);
      doReturn(100).when(stack).getData(DataComponentTypes.MAX_DAMAGE);
      doReturn(stack).when(state).getBase();
      stack = mock();
      doReturn(100).when(stack).getData(DataComponentTypes.DAMAGE);
      doReturn(stack).when(state).getAddition();

      AnvilFunctionResult<ItemStack> result = function.getResult(behavior, state, resultStack);
      assertThat("Combine repair costs 2", result.getLevelCostIncrease(), is(2));
      assertThat("Material cost is unchanged", result.getMaterialCostIncrease(), is(0));

      stack = mock();
      result.modifyResult(stack);
      verify(stack).setData(DataComponentTypes.DAMAGE, 88);
    }

  }

  @Test
  void gettersFetchConstants() {
    // A bit of a silly test, but might prevent accidents.
    ComponentAnvilFunctions functions = ComponentAnvilFunctions.INSTANCE;
    assertThat(
        "Provided function is constant",
        functions.addPriorWorkLevelCost(),
        is(sameInstance(ComponentAnvilFunctions.PRIOR_WORK_LEVEL_COST))
    );
    assertThat(
        "Provided function is constant",
        functions.rename(),
        is(sameInstance(ComponentAnvilFunctions.RENAME))
    );
    assertThat(
        "Provided function is constant",
        functions.setItemPriorWork(),
        is(sameInstance(ComponentAnvilFunctions.UPDATE_PRIOR_WORK_COST))
    );
    assertThat(
        "Provided function is constant",
        functions.repairWithMaterial(),
        is(sameInstance(ComponentAnvilFunctions.REPAIR_WITH_MATERIAL))
    );
    assertThat(
        "Provided function is constant",
        functions.repairWithCombine(),
        is(sameInstance(ComponentAnvilFunctions.REPAIR_WITH_COMBINATION))
    );
    assertThat(
        "Provided function is constant",
        functions.combineEnchantsJava(),
        is(sameInstance(ComponentAnvilFunctions.COMBINE_ENCHANTMENTS_JAVA))
    );
    assertThat(
        "Provided function is constant",
        functions.combineEnchantsBedrock(),
        is(sameInstance(ComponentAnvilFunctions.COMBINE_ENCHANTMENTS_BEDROCK))
    );
  }

}
