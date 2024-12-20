package com.github.jikoo.planarenchanting.anvil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import com.github.jikoo.planarenchanting.anvil.mock.ReadableResultState;
import com.github.jikoo.planarenchanting.enchant.EnchantData;
import com.github.jikoo.planarenchanting.enchant.EnchantmentUtil;
import com.github.jikoo.planarenchanting.util.mock.ServerMocks;
import com.github.jikoo.planarenchanting.util.mock.enchantments.EnchantmentMocks;
import com.github.jikoo.planarenchanting.util.mock.impl.InternalObject;
import com.github.jikoo.planarenchanting.util.mock.inventory.InventoryMocks;
import com.github.jikoo.planarenchanting.util.mock.inventory.ItemFactoryMocks;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.AnvilView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@DisplayName("Default enchanting AnvilFunctions")
@TestInstance(Lifecycle.PER_CLASS)
public class CombineEnchantmentsTest {

  private static final Material BASE_MAT = Material.DIAMOND_SHOVEL;
  private static Enchantment basicEnchant;
  private static Enchantment rareEnchant;
  private static Enchantment commonTridentEnchant;
  private static Enchantment tridentEnchant;

  @BeforeAll
  void beforeAll() {
    Server server = ServerMocks.mockServer();

    ItemFactory factory = ItemFactoryMocks.mockFactory();
    when(server.getItemFactory()).thenReturn(factory);

    EnchantmentMocks.init();

    Tag<Material> tag = Tag.ITEMS_ENCHANTABLE_TRIDENT;
    doReturn(Set.of(Material.TRIDENT)).when(tag).getValues();
    doReturn(true).when(tag).isTagged(Material.TRIDENT);

    basicEnchant = Enchantment.EFFICIENCY;
    rareEnchant = Enchantment.FORTUNE;
    tridentEnchant = Enchantment.RIPTIDE;

    // Set up a fake common-rarity trident enchantment.
    // This is necessary because no actual trident enchantments are currently common, so the
    // defensive code will not be hit in testing normally.
    var commonTrident = (Enchantment & InternalObject<?>) mock(Enchantment.class, withSettings().extraInterfaces(InternalObject.class));
    NamespacedKey key = NamespacedKey.minecraft("common_trident");
    doReturn(key).when(commonTrident).getKey();
    doReturn(
        new net.minecraft.world.item.enchantment.Enchantment(
            key,
            value -> 1,
            value -> 21,
            10,
            1
        )
    ).when(commonTrident).getHandle();
    EnchantmentMocks.putEnchant(commonTrident);
    commonTridentEnchant = commonTrident;
  }

  abstract static class CombineEnchantsTest {

    protected final AnvilFunction function;

    protected CombineEnchantsTest(AnvilFunction function) {
      this.function = function;
    }

    @Test
    void testAppliesIfNotCombine() {
      var anvil = getMockView(new ItemStack(BASE_MAT), new ItemStack(BASE_MAT));
      var behavior = spy(VanillaAnvil.BEHAVIOR);
      doReturn(false).when(behavior).itemsCombineEnchants(notNull(), notNull());
      var state = new AnvilState(behavior, anvil);

      assertThat("Combination must not apply", function.canApply(behavior, state), is(false));
    }

    @Test
    void testAppliesIfCombine() {
      var anvil = getMockView(new ItemStack(BASE_MAT), new ItemStack(BASE_MAT));
      var behavior = spy(VanillaAnvil.BEHAVIOR);
      doReturn(true).when(behavior).itemsCombineEnchants(notNull(), notNull());
      var state = new AnvilState(behavior, anvil);

      assertThat("Combination must apply", function.canApply(behavior, state));
    }

    @Test
    void testNoEnchantsAdded() {
      var anvil = getMockView(new ItemStack(BASE_MAT), new ItemStack(BASE_MAT));
      var behavior = spy(VanillaAnvil.BEHAVIOR);
      doReturn(true).when(behavior).itemsCombineEnchants(notNull(), notNull());
      var state = new AnvilState(behavior, anvil);

      assertThat(
          "No enchants added yields empty result",
          function.getResult(behavior, state),
          is(AnvilFunctionResult.EMPTY));
    }

    @Test
    void testBasicAdd() {
      Map<Enchantment, Integer> enchantments = new HashMap<>();
      enchantments.put(Enchantment.EFFICIENCY, 1);
      ItemStack addition = new ItemStack(BASE_MAT);
      applyEnchantments(addition, enchantments);

      var anvil = getMockView(new ItemStack(BASE_MAT), addition);
      var behavior = spy(VanillaAnvil.BEHAVIOR);
      doReturn(true).when(behavior).itemsCombineEnchants(notNull(), notNull());
      doReturn(true).when(behavior).enchantApplies(notNull(), notNull());
      doReturn((int) Short.MAX_VALUE).when(behavior).getEnchantMaxLevel(notNull());
      var state = new ReadableResultState(behavior, anvil);

      assertThat("Combination must apply", function.canApply(behavior, state));

      AnvilFunctionResult result = function.getResult(behavior, state);
      assertThat("Material cost is unchanged", result.getMaterialCostIncrease(), is(0));
      result.modifyResult(state.getResult().getMeta());

      assertThat("Enchantments must be added to result",
          EnchantmentUtil.getEnchants(state.getResult().getMeta()).entrySet(),
          both(everyItem(is(in(enchantments.entrySet())))).and(
              containsInAnyOrder(enchantments.entrySet().toArray())));

      assertDoesNotThrow(() -> result.modifyResult(null));
    }

    @Test
    void testBookHalfPrice() {
      EnchantData enchantData = EnchantData.of(rareEnchant);
      assertThat(
          "Enchantment must have rarity > 1",
          enchantData.getAnvilCost(),
          greaterThan(1));


      ItemStack base = new ItemStack(BASE_MAT);
      Map<Enchantment, Integer> enchantments = new HashMap<>();
      enchantments.put(rareEnchant, 1);
      ItemStack addition = new ItemStack(BASE_MAT);
      applyEnchantments(addition, enchantments);
      ItemStack bookAddition = new ItemStack(Material.ENCHANTED_BOOK);
      applyEnchantments(bookAddition, enchantments);

      var anvil = getMockView(base, addition);
      var bookAnvil = getMockView(base, bookAddition);

      var behavior = mock(AnvilBehavior.class);
      doReturn(true).when(behavior).itemsCombineEnchants(notNull(), notNull());
      doReturn(true).when(behavior).enchantApplies(notNull(), notNull());
      doReturn((int) Short.MAX_VALUE).when(behavior).getEnchantMaxLevel(notNull());

      var state = new ReadableResultState(behavior, anvil);
      var bookState = new ReadableResultState(behavior, bookAnvil);

      assertThat("Combination must apply", function.canApply(behavior, state));
      assertThat("Book combination must apply", function.canApply(behavior, bookState));

      AnvilFunctionResult result = function.getResult(behavior, state);
      assertThat("Material cost is unchanged", result.getMaterialCostIncrease(), is(0));
      result.modifyResult(state.getResult().getMeta());
      AnvilFunctionResult bookResult = function.getResult(behavior, bookState);
      assertThat("Book material cost is unchanged", bookResult.getMaterialCostIncrease(), is(0));
      bookResult.modifyResult(bookState.getResult().getMeta());

      assertThat("Enchantments must be added to result",
          EnchantmentUtil.getEnchants(state.getResult().getMeta()).entrySet(),
          both(everyItem(is(in(enchantments.entrySet())))).and(
              containsInAnyOrder(enchantments.entrySet().toArray())));
      assertThat("Enchantments must be added to book result",
          EnchantmentUtil.getEnchants(bookState.getResult().getMeta()).entrySet(),
          both(everyItem(is(in(enchantments.entrySet())))).and(
              containsInAnyOrder(enchantments.entrySet().toArray())));

      assertThat(
          "Cost of book application is half regular cost",
          bookResult.getLevelCostIncrease(),
          is(result.getLevelCostIncrease() / 2));
    }

    @Test
    void testCombineAdd() {
      Map<Enchantment, Integer> enchantments = new HashMap<>();
      enchantments.put(basicEnchant, 1);
      ItemStack base = new ItemStack(BASE_MAT);
      applyEnchantments(base, enchantments);
      ItemStack addition = new ItemStack(BASE_MAT);
      applyEnchantments(addition, enchantments);

      var anvil = getMockView(base, addition);
      var behavior = mock(AnvilBehavior.class);
      doReturn(true).when(behavior).itemsCombineEnchants(notNull(), notNull());
      doReturn(true).when(behavior).enchantApplies(notNull(), notNull());
      doReturn((int) Short.MAX_VALUE).when(behavior).getEnchantMaxLevel(notNull());
      var state = new ReadableResultState(behavior, anvil);

      assertThat("Combination must apply", function.canApply(behavior, state));

      AnvilFunctionResult result = function.getResult(behavior, state);
      assertThat("Material cost is unchanged", result.getMaterialCostIncrease(), is(0));
      result.modifyResult(state.getResult().getMeta());

      enchantments.put(basicEnchant, 2);

      assertThat("Enchantments must be merged in result",
          EnchantmentUtil.getEnchants(state.getResult().getMeta()).entrySet(),
          both(everyItem(is(in(enchantments.entrySet())))).and(
              containsInAnyOrder(enchantments.entrySet().toArray())));
      assertThat(
          "Cost must be expected value",
          result.getLevelCostIncrease(),
          is(getCombineAddCost()));
    }

    abstract int getCombineAddCost();

    @Test
    void testMergeInvalid() {
      Map<Enchantment, Integer> enchantments = new HashMap<>();
      enchantments.put(basicEnchant, 1);
      ItemStack base = new ItemStack(BASE_MAT);
      applyEnchantments(base, enchantments);
      ItemStack addition = new ItemStack(BASE_MAT);
      Map<Enchantment, Integer> invalidEnchants = new HashMap<>();
      invalidEnchants.put(rareEnchant, 1);
      applyEnchantments(addition, invalidEnchants);

      var anvil = getMockView(base, addition);
      var behavior = mock(AnvilBehavior.class);
      doReturn(true).when(behavior).itemsCombineEnchants(notNull(), notNull());
      doReturn(true).when(behavior).enchantApplies(notNull(), notNull());
      doReturn((int) Short.MAX_VALUE).when(behavior).getEnchantMaxLevel(notNull());
      doReturn(true).when(behavior).enchantsConflict(notNull(), notNull());
      var state = new ReadableResultState(behavior, anvil);

      assertThat("Combination must apply", function.canApply(behavior, state));

      AnvilFunctionResult result = function.getResult(behavior, state);
      assertThat("Material cost is unchanged", result.getMaterialCostIncrease(), is(0));
      result.modifyResult(state.getResult().getMeta());

      assertThat("Enchantments must not be merged in result",
          EnchantmentUtil.getEnchants(state.getResult().getMeta()).entrySet(),
          both(everyItem(is(in(enchantments.entrySet())))).and(
              containsInAnyOrder(enchantments.entrySet().toArray())));
      assertThat(
          "Cost must be expected value",
          result.getLevelCostIncrease(),
          is(getMergeInvalidCost()));
    }

    abstract int getMergeInvalidCost();

    @Test
    void testCommonTridentEnchant() {
      Map<Enchantment, Integer> enchantments = new HashMap<>();
      enchantments.put(commonTridentEnchant, 1);
      ItemStack addition = new ItemStack(BASE_MAT);
      applyEnchantments(addition, enchantments);
      ItemStack base = new ItemStack(BASE_MAT);

      var anvil = getMockView(base, addition);
      var behavior = mock(AnvilBehavior.class);
      doReturn(true).when(behavior).itemsCombineEnchants(notNull(), notNull());
      doReturn(true).when(behavior).enchantApplies(notNull(), notNull());
      doReturn((int) Short.MAX_VALUE).when(behavior).getEnchantMaxLevel(notNull());
      var state = new ReadableResultState(behavior, anvil);

      assertThat("Combination must apply", function.canApply(behavior, state));

      AnvilFunctionResult result = function.getResult(behavior, state);
      assertThat("Material cost is unchanged", result.getMaterialCostIncrease(), is(0));
      result.modifyResult(state.getResult().getMeta());

      assertThat("Enchantments must be the same in result",
          EnchantmentUtil.getEnchants(state.getResult().getMeta()).entrySet(),
          both(everyItem(is(in(enchantments.entrySet())))).and(
              containsInAnyOrder(enchantments.entrySet().toArray())));
      assertThat("Cost must be expected value", result.getLevelCostIncrease(), is(1));
    }

    @Test
    void testTridentEnchant() {
      Map<Enchantment, Integer> enchantments = new HashMap<>();
      enchantments.put(tridentEnchant, 1);
      ItemStack addition = new ItemStack(BASE_MAT);
      applyEnchantments(addition, enchantments);
      ItemStack base = new ItemStack(BASE_MAT);

      var anvil = getMockView(base, addition);
      var behavior = mock(AnvilBehavior.class);
      doReturn(true).when(behavior).itemsCombineEnchants(notNull(), notNull());
      doReturn(true).when(behavior).enchantApplies(notNull(), notNull());
      doReturn((int) Short.MAX_VALUE).when(behavior).getEnchantMaxLevel(notNull());
      var state = new ReadableResultState(behavior, anvil);

      assertThat("Combination must apply", function.canApply(behavior, state));

      AnvilFunctionResult result = function.getResult(behavior, state);
      assertThat("Material cost is unchanged", result.getMaterialCostIncrease(), is(0));
      result.modifyResult(state.getResult().getMeta());

      assertThat("Enchantments must be the same in result",
          EnchantmentUtil.getEnchants(state.getResult().getMeta()).entrySet(),
          both(everyItem(is(in(enchantments.entrySet())))).and(
              containsInAnyOrder(enchantments.entrySet().toArray())));
      assertThat(
          "Cost must be expected value",
          result.getLevelCostIncrease(),
          is(getTridentEnchantCost()));
    }

    abstract int getTridentEnchantCost();

  }

  @Nested
  class CombineEnchantsJavaEdition extends CombineEnchantsTest {

    protected CombineEnchantsJavaEdition() {
      super(AnvilFunctions.COMBINE_ENCHANTMENTS_JAVA_EDITION);
    }

    @Override
    int getCombineAddCost() {
      return 2;
    }

    @Override
    int getMergeInvalidCost() {
      return 1;
    }

    @Override
    int getTridentEnchantCost() {
      return 4;
    }

    @Test
    void testNegativeCost() {
      Map<Enchantment, Integer> enchantments = new HashMap<>();
      enchantments.put(basicEnchant, -2);
      ItemStack base = new ItemStack(BASE_MAT);
      applyEnchantments(base, enchantments);
      enchantments.put(basicEnchant, -1);
      ItemStack addition = new ItemStack(BASE_MAT);
      applyEnchantments(addition, enchantments);

      var anvil = getMockView(base, addition);
      var behavior = mock(AnvilBehavior.class);
      doReturn(true).when(behavior).itemsCombineEnchants(notNull(), notNull());
      doReturn(true).when(behavior).enchantApplies(notNull(), notNull());
      doReturn((int) Short.MAX_VALUE).when(behavior).getEnchantMaxLevel(notNull());
      var state = new ReadableResultState(behavior, anvil);

      assertThat("Combination must apply", function.canApply(behavior, state));

      AnvilFunctionResult result = function.getResult(behavior, state);
      assertThat("Material cost is unchanged", result.getMaterialCostIncrease(), is(0));
      result.modifyResult(state.getResult().getMeta());

      assertThat("Enchantments must be added to result",
          EnchantmentUtil.getEnchants(state.getResult().getMeta()).entrySet(),
          both(everyItem(is(in(enchantments.entrySet())))).and(
              containsInAnyOrder(enchantments.entrySet().toArray())));
      assertThat(
          "Cost must be maximum repair cost",
          result.getLevelCostIncrease(),
          is(anvil.getMaximumRepairCost()));
    }

  }

  @Nested
  class CombineEnchantsBedrockEdition extends CombineEnchantsTest {

    protected CombineEnchantsBedrockEdition() {
      super(AnvilFunctions.COMBINE_ENCHANTMENTS_BEDROCK_EDITION);
    }

    @Override
    int getCombineAddCost() {
      return 1;
    }

    @Override
    int getMergeInvalidCost() {
      return 0;
    }

    @Override
    int getTridentEnchantCost() {
      return 2;
    }

    @Test
    void testOldLevelHigher() {
      Map<Enchantment, Integer> enchantments = new HashMap<>();
      enchantments.put(basicEnchant, 1);
      ItemStack addition = new ItemStack(BASE_MAT);
      applyEnchantments(addition, enchantments);
      enchantments.put(basicEnchant, 2);
      ItemStack base = new ItemStack(BASE_MAT);
      applyEnchantments(base, enchantments);

      var anvil = getMockView(base, addition);
      var behavior = mock(AnvilBehavior.class);
      doReturn(true).when(behavior).itemsCombineEnchants(notNull(), notNull());
      doReturn(true).when(behavior).enchantApplies(notNull(), notNull());
      doReturn((int) Short.MAX_VALUE).when(behavior).getEnchantMaxLevel(notNull());
      var state = new ReadableResultState(behavior, anvil);

      assertThat("Combination must apply", function.canApply(behavior, state));

      AnvilFunctionResult result = function.getResult(behavior, state);
      assertThat("Material cost is unchanged", result.getMaterialCostIncrease(), is(0));
      result.modifyResult(state.getResult().getMeta());

      assertThat("Enchantments must be the same in result",
          EnchantmentUtil.getEnchants(state.getResult().getMeta()).entrySet(),
          both(everyItem(is(in(enchantments.entrySet())))).and(
              containsInAnyOrder(enchantments.entrySet().toArray())));
      assertThat("Cost must be unchanged", result.getLevelCostIncrease(), is(0));
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

  private static void applyEnchantments(
      @NotNull ItemStack itemStack,
      @NotNull Map<Enchantment, Integer> enchantments) {
    ItemMeta itemMeta = itemStack.getItemMeta();
    assertThat("Meta may not be null", itemMeta, notNullValue());
    BiConsumer<Enchantment, Integer> metaAddEnchant = addEnchant(itemMeta);
    enchantments.forEach(metaAddEnchant);
    itemStack.setItemMeta(itemMeta);
  }

  private static BiConsumer<Enchantment, Integer> addEnchant(@NotNull ItemMeta meta) {
    if (meta instanceof EnchantmentStorageMeta storageMeta) {
      return (enchantment, level) -> storageMeta.addStoredEnchant(enchantment, level, true);
    }

    return (enchantment, level) -> meta.addEnchant(enchantment, level, true);
  }

}
