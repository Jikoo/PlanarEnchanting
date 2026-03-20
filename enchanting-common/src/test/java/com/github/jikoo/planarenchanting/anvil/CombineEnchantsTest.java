package com.github.jikoo.planarenchanting.anvil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.github.jikoo.planarenchanting.anvil.CombineEnchants.MergeResult;
import com.github.jikoo.planarenchanting.anvil.CombineEnchants.Platform;
import com.github.jikoo.planarenchanting.util.EnchantData;
import com.github.jikoo.planarenchanting.util.EnchantmentAccess;
import com.github.jikoo.planarenchanting.util.EnchantDataService;

import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.view.AnvilView;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;

@NullMarked
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CombineEnchantsTest {

  private MockedStatic<Bukkit> bukkit;
  private EnchantmentAccess<Void> access;
  private AnvilBehavior<Void> behavior;
  private ViewState<Void> state;
  private Void resultStack;

  @BeforeAll
  void setUp() {
    bukkit = mockStatic(Bukkit.class);
    bukkit.when(() -> Bukkit.getRegistry(any())).thenAnswer(inv -> {
      Registry<Enchantment> registry = mock();
      doAnswer(invocation -> {
        NamespacedKey key = invocation.getArgument(0);
        Enchantment enchant = mock();
        doReturn(key).when(enchant).getKey();
        return enchant;
      }).when(registry).getOrThrow(any());
      return registry;
    });
  }

  @AfterAll
  void tearDown() {
    bukkit.close();
  }

  @BeforeEach
  void beforeEach() {
    access = mock();
    behavior = mock();
    state = mock();
    resultStack = mock();
  }

  @Test
  void applyCombineTrue() {
    CombineEnchants<Void> function = new CombineEnchants<>(Platform.JAVA, access);
    doReturn(true).when(behavior).itemsCombineEnchants(any(), any());

    assertThat("Function can apply", function.canApply(behavior, state, resultStack), is(true));
  }

  @Test
  void applyCombineFalse() {
    CombineEnchants<Void> function = new CombineEnchants<>(Platform.JAVA, access);

    assertThat("Function cannot apply", function.canApply(behavior, state, resultStack), is(false));
  }

  @Test
  void getLevelCostEmpty() {
    CombineEnchants<Void> function = new CombineEnchants<>(Platform.JAVA, access);
    MergeResult result = function.getLevelCost(behavior, state, Map.of(), Map.of());
    assertThat(
        "Enchantment map for no enchantments is empty",
        result.enchantments(),
        is(anEmptyMap())
    );
    assertThat("Level cost for no enchantments is zero", result.levelCost(), is(0));
  }

  @ParameterizedTest
  @CsvSource({"JAVA,1", "BEDROCK,0"})
  void getLevelCostInapplicableBehavior(Platform platform, int cost) {
    Map<Enchantment, Integer> added = Map.of(mock(), 0);

    CombineEnchants<Void> function = new CombineEnchants<>(platform, access);
    MergeResult result = function.getLevelCost(behavior, state, Map.of(), added);

    assertThat(
        "Enchantment map for an inapplicable enchant is empty",
        result.enchantments(),
        is(anEmptyMap())
    );
    assertThat(
        "Level cost for an inapplicable enchant is expected value",
        result.levelCost(),
        is(cost)
    );
  }

  @ParameterizedTest
  @CsvSource({"JAVA,1", "BEDROCK,0"})
  void getLevelCostInapplicableConflict(Platform platform, int cost) {
    doReturn(true).when(behavior).enchantApplies(any(), any());
    doReturn(true).when(behavior).enchantsConflict(any(), any());

    Enchantment enchantment = mock();
    doReturn(NamespacedKey.minecraft("a")).when(enchantment).getKey();
    Map<Enchantment, Integer> base = Map.of(enchantment, 0);
    enchantment = mock();
    doReturn(NamespacedKey.minecraft("b")).when(enchantment).getKey();
    Map<Enchantment, Integer> added = Map.of(enchantment, 0);

    CombineEnchants<Void> function = new CombineEnchants<>(platform, access);
    MergeResult result = function.getLevelCost(behavior, state, base, added);

    assertThat(
        "Enchantment map for an inapplicable enchant is unchanged",
        result.enchantments(),
        is(base)
    );
    assertThat(
        "Level cost for an inapplicable enchant is expected value",
        result.levelCost(),
        is(cost)
    );
  }

  @Test
  void getLevelCostNonconflictingAdded() {
    doReturn(true).when(behavior).enchantApplies(any(), any());

    Enchantment baseEnchant = mock();
    doReturn(NamespacedKey.minecraft("a")).when(baseEnchant).getKey();
    Map<Enchantment, Integer> base = Map.of(baseEnchant, 0);
    Enchantment addedEnchant = mock();
    doReturn(NamespacedKey.minecraft("b")).when(addedEnchant).getKey();
    Map<Enchantment, Integer> added = Map.of(addedEnchant, 0);

    CombineEnchants<Void> function = new CombineEnchants<>(Platform.JAVA, access);
    MergeResult result = function.getLevelCost(behavior, state, base, added);

    assertThat(
        "Nonconflicting enchantment is added",
        result.enchantments(),
        is(allOf(aMapWithSize(2), hasKey(baseEnchant), hasKey(addedEnchant)))
    );
  }

  @ParameterizedTest
  @CsvSource({"JAVA,10,true", "JAVA,10,false", "BEDROCK,5,true", "BEDROCK,5,false"})
  void getLevelCostMerge(Platform platform, int cost, boolean conflict) {
    doReturn(true).when(behavior).enchantApplies(any(), any());
    doReturn(2).when(behavior).getEnchantMaxLevel(any());
    doReturn(conflict).when(behavior).enchantsConflict(any(), any());

    Enchantment enchantment = mock();
    doReturn(NamespacedKey.minecraft("a")).when(enchantment).getKey();

    Map<Enchantment, Integer> base = Map.of(enchantment, 1);
    Map<Enchantment, Integer> added = Map.of(enchantment, 1);

    EnchantData data = EnchantDataService.PROVIDER.of(enchantment);
    doReturn(5).when(data).getAnvilCost();

    CombineEnchants<Void> function = new CombineEnchants<>(platform, access);
    MergeResult result = function.getLevelCost(behavior, state, base, added);

    assertThat(
        "Enchantment entries were merged",
        result.enchantments(),
        is(allOf(not(base), aMapWithSize(base.size()), hasEntry(enchantment, 2)))
    );
    assertThat(
        "Level cost for enchantment merge is expected value",
        result.levelCost(),
        is(cost)
    );
  }

  @ParameterizedTest
  @CsvSource({"JAVA,2,1,2", "JAVA,1,2,2", "BEDROCK,2,1,0", "BEDROCK,1,2,1"})
  void getLevelCostUsesHigher(
      Platform platform,
      int baseLevel,
      int addedLevel,
      int levelsCharged
  ) {
    doReturn(true).when(behavior).enchantApplies(any(), any());
    doReturn(2).when(behavior).getEnchantMaxLevel(any());

    Enchantment enchantment = mock();
    doReturn(NamespacedKey.minecraft("a")).when(enchantment).getKey();

    Map<Enchantment, Integer> base = Map.of(enchantment, baseLevel);
    Map<Enchantment, Integer> added = Map.of(enchantment, addedLevel);

    EnchantData data = EnchantDataService.PROVIDER.of(enchantment);
    int anvilCost = 5;
    doReturn(anvilCost).when(data).getAnvilCost();

    CombineEnchants<Void> function = new CombineEnchants<>(platform, access);
    MergeResult result = function.getLevelCost(behavior, state, base, added);

    int max = Math.max(baseLevel, addedLevel);
    assertThat(
        "Enchantment entries were merged",
        result.enchantments(),
        is(both(aMapWithSize(base.size())).and(hasEntry(enchantment, max)))
    );
    assertThat(
        "Level cost for enchantment merge is expected value",
        result.levelCost(),
        is(levelsCharged * anvilCost)
    );
  }

  @ParameterizedTest
  @CsvSource({"1,3,2", "2,2,1"})
  void getLevelCostCapsToMax(int baseLevel, int addedLevel, int maxLevel) {
    doReturn(true).when(behavior).enchantApplies(any(), any());
    doReturn(maxLevel).when(behavior).getEnchantMaxLevel(any());

    Enchantment enchantment = mock();
    doReturn(NamespacedKey.minecraft("a")).when(enchantment).getKey();

    Map<Enchantment, Integer> base = Map.of(enchantment, baseLevel);
    Map<Enchantment, Integer> added = Map.of(enchantment, addedLevel);

    EnchantData data = EnchantDataService.PROVIDER.of(enchantment);
    int anvilCost = 5;
    doReturn(anvilCost).when(data).getAnvilCost();

    CombineEnchants<Void> function = new CombineEnchants<>(Platform.JAVA, access);
    MergeResult result = function.getLevelCost(behavior, state, base, added);

    assertThat(
        "Enchantment entries were merged",
        result.enchantments(),
        is(both(aMapWithSize(1)).and(hasEntry(enchantment, maxLevel)))
    );
    assertThat(
        "Level cost for enchantment merge is expected value",
        result.levelCost(),
        is(maxLevel * anvilCost)
    );
  }

  @ParameterizedTest
  @EnumSource(Platform.class)
  void getLevelCostBook(Platform platform) {
    doReturn(true).when(behavior).enchantApplies(any(), any());
    doReturn(1).when(behavior).getEnchantMaxLevel(any());
    doReturn(true).when(access).isBook(any());

    Enchantment enchantment = mock();
    doReturn(NamespacedKey.minecraft("a")).when(enchantment).getKey();

    Map<Enchantment, Integer> base = Map.of();
    Map<Enchantment, Integer> added = Map.of(enchantment, 1);

    EnchantData data = EnchantDataService.PROVIDER.of(enchantment);
    int anvilCost = 5;
    doReturn(anvilCost).when(data).getAnvilCost();

    CombineEnchants<Void> function = new CombineEnchants<>(platform, access);
    MergeResult result = function.getLevelCost(behavior, state, base, added);

    assertThat(
        "Enchantment was added",
        result.enchantments(),
        is(both(aMapWithSize(1)).and(hasEntry(enchantment, 1)))
    );
    assertThat(
        "Level cost is expected value",
        result.levelCost(),
        is(2)
    );
  }

  @Test
  void getLevelCostBedrockTrident() {
    doReturn(true).when(behavior).enchantApplies(any(), any());
    doReturn(1).when(behavior).getEnchantMaxLevel(any());

    Enchantment enchantment = mock();
    doReturn(NamespacedKey.minecraft("a")).when(enchantment).getKey();

    Map<Enchantment, Integer> base = Map.of();
    Map<Enchantment, Integer> added = Map.of(enchantment, 1);

    EnchantData data = EnchantDataService.PROVIDER.of(enchantment);
    int anvilCost = 5;
    doReturn(anvilCost).when(data).getAnvilCost();
    doReturn(true).when(data).isTridentEnchant();

    CombineEnchants<Void> function = new CombineEnchants<>(Platform.BEDROCK, access);
    MergeResult result = function.getLevelCost(behavior, state, base, added);

    assertThat(
        "Enchantment was added",
        result.enchantments(),
        is(both(aMapWithSize(1)).and(hasEntry(enchantment, 1)))
    );
    assertThat(
        "Level cost is expected value",
        result.levelCost(),
        is(2)
    );
  }

  @Test
  void getResultNoAddedEnchants() {
    CombineEnchants<Void> function = new CombineEnchants<>(Platform.JAVA, access);
    doReturn(Map.of()).when(access).getEnchantments(any());

    assertThat(
        "No enchants added yields empty result",
        function.getResult(behavior, state, resultStack),
        is(AnvilFunctionResult.empty())
    );
  }

  @Test
  void getResult() {
    doReturn(true).when(behavior).enchantApplies(any(), any());
    doReturn(1).when(behavior).getEnchantMaxLevel(any());

    Enchantment enchantment = mock();
    EnchantData data = EnchantDataService.PROVIDER.of(enchantment);
    doReturn(5).when(data).getAnvilCost();
    doReturn(Map.of()).doReturn(Map.of(enchantment, 1)).when(access).getEnchantments(any());

    CombineEnchants<Void> function = new CombineEnchants<>(Platform.JAVA, access);
    AnvilFunctionResult<Void> result = function.getResult(behavior, state, resultStack);

    assertThat(
        "Result is not empty",
        result,
        is(not(AnvilFunctionResult.empty()))
    );
    assertThat("Final cost is expected", result.getLevelCostIncrease(), is(5));
    assertThat("Material cost is not specified", result.getMaterialCostIncrease(), is(0));

    verify(access, never()).addEnchantments(any(), any());
    result.modifyResult(resultStack);
    verify(access).addEnchantments(any(), any());
  }

  @Test
  void getResultNegative() {
    doReturn(true).when(behavior).enchantApplies(any(), any());

    AnvilView view = mock();
    doReturn(99).when(view).getMaximumRepairCost();
    doReturn(view).when(state).getAnvilView();

    Enchantment enchantment = mock();
    EnchantData data = EnchantDataService.PROVIDER.of(enchantment);
    doReturn(5).when(data).getAnvilCost();
    doReturn(Map.of(enchantment, -2)).when(access).getEnchantments(any());

    CombineEnchants<Void> function = new CombineEnchants<>(Platform.JAVA, access);
    AnvilFunctionResult<Void> result = function.getResult(behavior, state, resultStack);

    assertThat(
        "Result is not empty",
        result,
        is(not(AnvilFunctionResult.empty()))
    );
    assertThat("Final cost is expected", result.getLevelCostIncrease(), is(99));
  }

}
