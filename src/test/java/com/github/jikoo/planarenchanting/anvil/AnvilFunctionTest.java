package com.github.jikoo.planarenchanting.anvil;

import static com.github.jikoo.planarenchanting.util.matcher.ItemMatcher.isMetaEqual;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.github.jikoo.planarenchanting.util.mock.ServerMocks;
import com.github.jikoo.planarenchanting.util.mock.enchantments.EnchantmentMocks;
import com.github.jikoo.planarenchanting.util.mock.inventory.InventoryMocks;
import com.github.jikoo.planarenchanting.util.mock.inventory.ItemFactoryMocks;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.view.AnvilView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Default basic AnvilFunctions")
@TestInstance(Lifecycle.PER_CLASS)
class AnvilFunctionTest {

  private static final Material BASE_MAT = Material.DIAMOND_SHOVEL;
  private static final int MAX_DAMAGE = BASE_MAT.getMaxDurability() - 1;
  private static final Material REPAIR_MAT = Material.DIAMOND;

  @BeforeAll
  void beforeAll() {
    Server server = ServerMocks.mockServer();

    ItemFactory factory = ItemFactoryMocks.mockFactory();
    when(server.getItemFactory()).thenReturn(factory);

    // RepairMaterial requires these tags to be set up to test.
    Tag<Material> tag = Tag.ITEMS_STONE_TOOL_MATERIALS;
    doReturn(Set.of(Material.STONE, Material.ANDESITE, Material.GRANITE, Material.DIORITE))
        .when(tag).getValues();
    tag = Tag.PLANKS;
    doReturn(Set.of(Material.ACACIA_PLANKS, Material.BIRCH_PLANKS, Material.OAK_PLANKS)) //etc. non-exhaustive list
        .when(tag).getValues();

    EnchantmentMocks.init();
  }

  @Nested
  class PriorWorkLevelCost {

    private final AnvilFunction function =  AnvilFunctions.PRIOR_WORK_LEVEL_COST;

    @Test
    void testPriorWorkLevelCostApplies() {
      var anvil = getMockView(new ItemStack(BASE_MAT), new ItemStack(BASE_MAT));
      var behavior = AnvilBehavior.VANILLA;
      var state = new AnvilState(anvil);

      assertThat("Prior work level cost always applies", function.canApply(behavior, state));
    }

    @ParameterizedTest
    @MethodSource("com.github.jikoo.planarenchanting.anvil.AnvilFunctionTest#getPriorWork")
    void testPriorWorkLevelCostValues(int baseWork, int addedWork) {
      var baseItem = new ItemStack(BASE_MAT);
      var anvil = getMockView(
          prepareItem(baseItem, 0, baseWork),
          prepareItem(new ItemStack(BASE_MAT), 0, addedWork));
      var behavior = AnvilBehavior.VANILLA;
      var state = new AnvilState(anvil);

      assertThat("Prior work level cost always applies", function.canApply(behavior, state));

      var result = function.getResult(behavior, state);
      assertThat(
          "Cost must be total prior work",
          result.getLevelCostIncrease(),
          is(baseWork + addedWork));

      result.modifyResult(state.result.getMeta());
      assertThat(
          "Meta must not be changed",
          state.result.getMeta(),
          isMetaEqual(Objects.requireNonNull(baseItem.getItemMeta())));
      assertThat("Material cost is unchanged", result.getMaterialCostIncrease(), is(0));
    }

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

  @Nested
  @TestInstance(Lifecycle.PER_CLASS) // Should be inherited but isn't for whatever reason
  class Rename {

    private final AnvilFunction function = AnvilFunctions.RENAME;

    @DisplayName("Rename requires ItemMeta")
    @Test
    void testRenameRequiresMeta() {
      var base = getNullMetaItem();
      var inventory = getMockView(base, null);
      var behavior = AnvilBehavior.VANILLA;
      var state = new AnvilState(inventory);

      assertThat("Base meta is null", state.getBase().getMeta(), is(nullValue()));
      assertThat("Rename requires meta", function.canApply(behavior, state), is(false));
    }

    @DisplayName("Rename requires different name")
    @ParameterizedTest
    @MethodSource("renameSituations")
    void testRenameRequiresDifferentName(
        BiConsumer<ItemMeta, AnvilView> setup,
        boolean canApply) {
      var base = new ItemStack(BASE_MAT);
      var inventory = getMockView(base, null);
      var behavior = AnvilBehavior.VANILLA;
      var state = new AnvilState(inventory);

      assertThat("Base meta is not null", state.getBase().getMeta(), is(notNullValue()));

      setup.accept(state.getBase().getMeta(), inventory);

      assertThat("Rename requires different name", function.canApply(behavior, state), is(canApply));
    }

    @DisplayName("Rename applies name and cost")
    @ParameterizedTest
    @MethodSource("renameSuccessSituations")
    void testRenameApplication(BiConsumer<ItemMeta, AnvilView> setup) {
      var base = new ItemStack(BASE_MAT);
      var baseMeta = base.getItemMeta();
      assertThat("Base meta is not null", baseMeta, is(notNullValue()));
      var inventory = getMockView(base, null);
      setup.accept(baseMeta, inventory);
      base.setItemMeta(baseMeta);
      inventory.setItem(0, base);

      var behavior = AnvilBehavior.VANILLA;
      var state = new AnvilState(inventory);

      AnvilFunctionResult result = function.getResult(behavior, state);
      assertThat("Level cost increase is 1", result.getLevelCostIncrease(), is(1));

      result.modifyResult(state.result.getMeta());
      assertThat(
          "Meta must be changed",
          state.result.getMeta(),
          not(isMetaEqual(base.getItemMeta())));
      assertThat("Material cost is unchanged", result.getMaterialCostIncrease(), is(0));
    }

    private Stream<Arguments> renameSuccessSituations() {
      return renameSituations().filter(arguments -> (boolean) arguments.get()[1]);
    }

    private Stream<Arguments> renameSituations() {
      String displayName = "Sample text";
      return Stream.of(
          // NON-APPLICABLE
          // Both unnamed
          Arguments.of((BiConsumer<ItemMeta, AnvilView>) (meta, anvil) -> {
            meta.setDisplayName(null);
            when(anvil.getRenameText()).thenReturn(null);
          }, false),
          // Both identically named
          Arguments.of((BiConsumer<ItemMeta, AnvilView>) (meta, anvil) -> {
            meta.setDisplayName(displayName);
            when(anvil.getRenameText()).thenReturn(displayName);
          }, false),

          // APPLICABLE
          // Only anvil named
          Arguments.of((BiConsumer<ItemMeta, AnvilView>) (meta, anvil) -> {
            meta.setDisplayName(null);
            when(anvil.getRenameText()).thenReturn(displayName);
          }, true),
          // Only item named
          Arguments.of((BiConsumer<ItemMeta, AnvilView>) (meta, anvil) -> {
            meta.setDisplayName(displayName);
            when(anvil.getRenameText()).thenReturn(null);
          }, true),
          // Both named differently
          Arguments.of((BiConsumer<ItemMeta, AnvilView>) (meta, anvil) -> {
            meta.setDisplayName(displayName);
            when(anvil.getRenameText()).thenReturn(displayName + " different text");
          }, true)
      );
    }

  }

  @Nested
  class UpdatePriorWorkCost {

    private final AnvilFunction function =  AnvilFunctions.UPDATE_PRIOR_WORK_COST;

    @Test
    void testBaseNotRepairable() {
      var base = getNullMetaItem();
      var inventory = getMockView(base, null);
      var behavior = AnvilBehavior.VANILLA;
      var state = new AnvilState(inventory);

      assertThat(
          "Base must be repairable",
          function.canApply(behavior, state),
          is(false));
    }

    @Test
    void testBaseRepairable() {
      var base = new ItemStack(BASE_MAT);
      var addition = getNullMetaItem();
      var inventory = getMockView(base, addition);
      var behavior = AnvilBehavior.VANILLA;
      var state = new AnvilState(inventory);

      assertThat(
          "Repairable base is acceptable",
          function.canApply(behavior, state),
          is(true));
    }

    @ParameterizedTest
    @MethodSource("com.github.jikoo.planarenchanting.anvil.AnvilFunctionTest#getPriorWork")
    void testPriorWorkUpdate(int baseWork, int addedWork) {
      var baseItem = new ItemStack(BASE_MAT);
      var anvil = getMockView(
          prepareItem(baseItem, 0, baseWork),
          prepareItem(new ItemStack(BASE_MAT), 0, addedWork));
      var behavior = AnvilBehavior.VANILLA;
      var state = new AnvilState(anvil);

      assertThat("Prior work level cost always applies", function.canApply(behavior, state));

      var result = function.getResult(behavior, state);
      assertThat("Cost must be unchanged", result.getLevelCostIncrease(), is(0));
      assertThat("Material cost is unchanged", result.getMaterialCostIncrease(), is(0));

      ItemMeta resultMeta = state.result.getMeta();
      result.modifyResult(resultMeta);
      int resultPriorWork = requireRepairable(resultMeta).getRepairCost();

      int expectedPriorWork = 1 + (Math.max(baseWork, addedWork) * 2);
      assertThat("Prior work must be expected value", resultPriorWork, is(expectedPriorWork));

      assertThat(
          "Repair cost must be changed",
          resultPriorWork,
          is(greaterThan(requireRepairable(baseItem.getItemMeta()).getRepairCost())));
      assertThat(
          "Meta must be changed",
          state.result.getMeta(),
          not(isMetaEqual(Objects.requireNonNull(baseItem.getItemMeta()))));
    }

    @Test
    void testMetaNotRepairable() {
      var base = getNullMetaItem();
      var inventory = getMockView(base, null);
      var behavior = AnvilBehavior.VANILLA;
      var state = new AnvilState(inventory);

      var result = function.getResult(behavior, state);
      assertDoesNotThrow(() -> result.modifyResult(base.getItemMeta()));
    }

  }

  @Nested
  class RepairWithMaterial {

    private final AnvilFunction function = AnvilFunctions.REPAIR_WITH_MATERIAL;

    @Test
    void testCanApplyNotRepairedBy() {
      var inventory = getMockView(null, null);
      var behavior = spy(AnvilBehavior.VANILLA);
      doReturn(false).when(behavior).itemRepairedBy(notNull(), notNull());
      var state = new AnvilState(inventory);

      assertThat(
          "Must be repairable by item",
          function.canApply(behavior, state),
          is(false));
    }

    @Test
    void testCanApplyNotDurable() {
      var inventory = getMockView(null, null);
      var behavior = spy(AnvilBehavior.VANILLA);
      doReturn(true).when(behavior).itemRepairedBy(notNull(), notNull());
      var state = new AnvilState(inventory);

      assertThat(
          "Must have durability",
          function.canApply(behavior, state),
          is(false));
    }

    @Test
    void testCanApplyNotDamageable() {
      var base = getNullMetaItem();
      base.setType(BASE_MAT);
      var inventory = getMockView(base, null);
      var behavior = spy(AnvilBehavior.VANILLA);
      doReturn(true).when(behavior).itemRepairedBy(notNull(), notNull());
      var state = new AnvilState(inventory);

      assertThat(
          "Must have Damageable meta",
          function.canApply(behavior, state),
          is(false));
      assertThat(
          "Non-Damageable must return empty result",
          function.getResult(behavior, state),
          is(AnvilFunctionResult.EMPTY));
    }

    @Test
    void testCanApplyNotDamaged() {
      var inventory = getMockView(new ItemStack(BASE_MAT), null);
      var behavior = spy(AnvilBehavior.VANILLA);
      doReturn(true).when(behavior).itemRepairedBy(notNull(), notNull());
      var state = new AnvilState(inventory);

      assertThat(
          "Must be damaged",
          function.canApply(behavior, state),
          is(false));
      assertThat(
          "Undamaged must return empty result",
          function.getResult(behavior, state),
          is(AnvilFunctionResult.EMPTY));
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 64 })
    void testRepair(int repairMats) {
      var baseItem = getMaxDamageItem();
      var inventory = getMockView(baseItem, new ItemStack(REPAIR_MAT, repairMats));
      var behavior = spy(AnvilBehavior.VANILLA);
      doReturn(true).when(behavior).itemRepairedBy(notNull(), notNull());
      var state = new AnvilState(inventory);

      assertThat(
          "Must be applicable",
          function.canApply(behavior, state),
          is(true));

      AnvilFunctionResult result = function.getResult(behavior, state);

      result.modifyResult(state.result.getMeta());

      int damage = requireDamageable(state.result.getMeta()).getDamage();

      assertThat(
          "Material cost must be specified",
          result.getMaterialCostIncrease(),
          greaterThan(0));
      assertThat("Item must be repaired",
          damage,
          is(Math.max(
              0,
              MAX_DAMAGE - result.getMaterialCostIncrease() * (BASE_MAT.getMaxDurability() / 4))));
      assertThat(
          "Number of items to consume should not exceed number of available items",
          result.getMaterialCostIncrease(),
          lessThanOrEqualTo(repairMats));
      assertThat(
          "Level cost is material cost",
          result.getLevelCostIncrease(),
          is(result.getMaterialCostIncrease()));
      assertThat(
          "Meta must be changed",
          state.result.getMeta(),
          not(isMetaEqual(Objects.requireNonNull(baseItem.getItemMeta()))));

      assertDoesNotThrow(() -> result.modifyResult(null));
    }

  }

  @Nested
  class RepairWithCombination {

    private final AnvilFunction function = AnvilFunctions.REPAIR_WITH_COMBINATION;

    @Test
    void testCanApplyNotRepairedBy() {
      var inventory = getMockView(new ItemStack(BASE_MAT), new ItemStack(REPAIR_MAT));
      var behavior = AnvilBehavior.VANILLA;
      var state = new AnvilState(inventory);

      assertThat(
          "Must be same item",
          function.canApply(behavior, state),
          is(false));
    }

    @Test
    void testCanApplyNotDurable() {
      var inventory = getMockView(null, null);
      var behavior = AnvilBehavior.VANILLA;
      var state = new AnvilState(inventory);

      assertThat(
          "Must have durability",
          function.canApply(behavior, state),
          is(false));
    }

    @Test
    void testCanApplyNotDamageable() {
      var nullMetaItem = getNullMetaItem();
      nullMetaItem.setType(BASE_MAT);
      ItemStack normalItem = new ItemStack(BASE_MAT);
      var inventory = getMockView(nullMetaItem, normalItem);
      var behavior = AnvilBehavior.VANILLA;
      var state = new AnvilState(inventory);

      assertThat(
          "Must have Damageable meta",
          function.canApply(behavior, state),
          is(false));
      assertThat(
          "Non-Damageable base must return empty result",
          function.getResult(behavior, state),
          is(AnvilFunctionResult.EMPTY));
      inventory = getMockView(normalItem, nullMetaItem);
      state = new AnvilState(inventory);
      assertThat(
          "Non-Damageable addition must return empty result",
          function.getResult(behavior, state),
          is(AnvilFunctionResult.EMPTY));
    }

    @Test
    void testCanApplyNotDamaged() {
      var inventory = getMockView(new ItemStack(BASE_MAT), new ItemStack(BASE_MAT));
      var behavior = AnvilBehavior.VANILLA;
      var state = new AnvilState(inventory);

      assertThat(
          "Must be damaged",
          function.canApply(behavior, state),
          is(false));
      assertThat(
          "Undamaged must return empty result",
          function.getResult(behavior, state),
          is(AnvilFunctionResult.EMPTY));
    }

    @Test
    void testRepair() {
      var baseItem = getMaxDamageItem();
      var inventory = getMockView(baseItem, baseItem.clone());
      var behavior = AnvilBehavior.VANILLA;
      var state = new AnvilState(inventory);

      assertThat(
          "Must be applicable",
          function.canApply(behavior, state),
          is(true));

      AnvilFunctionResult result = function.getResult(behavior, state);

      result.modifyResult(state.result.getMeta());

      assertThat("Level cost is 2", result.getLevelCostIncrease(), is(2));
      assertThat("Material cost is unchanged", result.getMaterialCostIncrease(), is(0));
      assertThat(
          "Meta must be changed",
          state.result.getMeta(),
          not(isMetaEqual(Objects.requireNonNull(baseItem.getItemMeta()))));

      int maxDurability = state.result.getItem().getType().getMaxDurability();
      int remainingDurability = maxDurability - requireDamageable(baseItem.getItemMeta()).getDamage();
      int bonusDurability = maxDurability * 12 / 100;
      int expectedDurability = 2 * remainingDurability + bonusDurability;
      int expectedDamage = maxDurability - expectedDurability;
      final int damage = requireDamageable(state.result.getMeta()).getDamage();

      assertThat(
          "Items' durability should be added with a bonus of 12% of max durability",
          damage,
          is(expectedDamage));
      assertDoesNotThrow(() -> result.modifyResult(null));
    }

  }

  private static @NotNull AnvilView getMockView(
      @Nullable ItemStack base,
      @Nullable ItemStack addition) {
    var anvil = InventoryMocks.newAnvilMock();
    anvil.setItem(0, base);
    anvil.setItem(1, addition);

    var view = mock(AnvilView.class);
    doAnswer(params -> anvil.getItem(params.getArgument(0)))
        .when(view).getItem(anyInt());
    doAnswer(params -> {
      anvil.setItem(params.getArgument(0), params.getArgument(1));
      return null;
    }).when(view).setItem(anyInt(), any());

    return view;
  }

  private static ItemStack getNullMetaItem() {
    return new ItemStack(Material.AIR) {
      @Override
      public @Nullable ItemMeta getItemMeta() {
        // ItemFactoryMock incorrectly returns non-null meta for AIR ItemStacks.
        return null;
      }
    };
  }

  private static ItemStack getMaxDamageItem() {
    return prepareItem(new ItemStack(BASE_MAT), MAX_DAMAGE, 0);
  }

  private static ItemStack prepareItem(ItemStack itemStack, int damage, int repairCost) {
    ItemMeta itemMeta = itemStack.getItemMeta();

    if (damage != 0) {
      Damageable damageable = requireDamageable(itemMeta);
      damageable.setDamage(damage);
    }

    if (repairCost != 0) {
      Repairable repairable = requireRepairable(itemMeta);
      repairable.setRepairCost(repairCost);
    }

    itemStack.setItemMeta(itemMeta);

    return itemStack;
  }

  private static Damageable requireDamageable(@Nullable ItemMeta itemMeta) {
    assertThat("Meta may not be null", itemMeta, notNullValue());
    assertThat("Item must be damageable", itemMeta, instanceOf(Damageable.class));

    return (Damageable) itemMeta;
  }

  private static Repairable requireRepairable(@Nullable ItemMeta itemMeta) {
    assertThat("Meta may not be null", itemMeta, notNullValue());
    assertThat("Item must be repairable", itemMeta, instanceOf(Repairable.class));

    return (Repairable) itemMeta;
  }

}