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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
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
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
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
      var anvil = getMockInventory(new ItemStack(BASE_MAT), new ItemStack(BASE_MAT));
      var operation = new AnvilOperation();
      operation.setItemsCombineEnchants(((itemStack, itemStack2) -> false));
      var state = new AnvilOperationState(operation, anvil);

      assertThat("Combination must not apply", function.canApply(operation, state), is(false));
    }

    @Test
    void testAppliesIfCombine() {
      var anvil = getMockInventory(new ItemStack(BASE_MAT), new ItemStack(BASE_MAT));
      var operation = new AnvilOperation();
      operation.setItemsCombineEnchants(((itemStack, itemStack2) -> true));
      var state = new AnvilOperationState(operation, anvil);

      assertThat("Combination must apply", function.canApply(operation, state));
    }

    @Test
    void testNoEnchantsAdded() {
      var anvil = getMockInventory(new ItemStack(BASE_MAT), new ItemStack(BASE_MAT));
      var operation = new AnvilOperation();
      operation.setItemsCombineEnchants(((itemStack, itemStack2) -> true));
      var state = new AnvilOperationState(operation, anvil);

      assertThat(
          "No enchants added yields empty result",
          function.getResult(operation, state),
          is(AnvilFunctionResult.EMPTY));
    }

    @Test
    void testBasicAdd() {
      Map<Enchantment, Integer> enchantments = new HashMap<>();
      enchantments.put(Enchantment.EFFICIENCY, 1);
      ItemStack addition = new ItemStack(BASE_MAT);
      applyEnchantments(addition, enchantments);

      var anvil = getMockInventory(new ItemStack(BASE_MAT), addition);
      var operation = new AnvilOperation();
      operation.setItemsCombineEnchants(((itemStack, itemStack2) -> true));
      operation.setEnchantApplies((enchantment, item) -> true);
      operation.setEnchantMaxLevel(enchantment -> Short.MAX_VALUE);
      var state = new ReadableResultState(operation, anvil);

      assertThat("Combination must apply", function.canApply(operation, state));

      AnvilFunctionResult result = function.getResult(operation, state);
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

      var anvil = getMockInventory(base, addition);
      var bookAnvil = getMockInventory(base, bookAddition);

      var operation = new AnvilOperation();
      operation.setItemsCombineEnchants(((itemStack, itemStack2) -> true));
      operation.setEnchantApplies((enchantment, item) -> true);
      operation.setEnchantMaxLevel(enchantment -> Short.MAX_VALUE);
      operation.setEnchantsConflict((enchantment1, enchantment2) -> false);

      var state = new ReadableResultState(operation, anvil);
      var bookState = new ReadableResultState(operation, bookAnvil);

      assertThat("Combination must apply", function.canApply(operation, state));
      assertThat("Book combination must apply", function.canApply(operation, bookState));

      AnvilFunctionResult result = function.getResult(operation, state);
      assertThat("Material cost is unchanged", result.getMaterialCostIncrease(), is(0));
      result.modifyResult(state.getResult().getMeta());
      AnvilFunctionResult bookResult = function.getResult(operation, bookState);
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

      var anvil = getMockInventory(base, addition);
      var operation = new AnvilOperation();
      operation.setItemsCombineEnchants(((itemStack, itemStack2) -> true));
      operation.setEnchantApplies((enchantment, item) -> true);
      operation.setEnchantMaxLevel(enchantment -> Short.MAX_VALUE);
      operation.setEnchantsConflict((enchantment1, enchantment2) -> false);
      var state = new ReadableResultState(operation, anvil);

      assertThat("Combination must apply", function.canApply(operation, state));

      AnvilFunctionResult result = function.getResult(operation, state);
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

      var anvil = getMockInventory(base, addition);
      var operation = new AnvilOperation();
      operation.setItemsCombineEnchants(((itemStack, itemStack2) -> true));
      operation.setEnchantApplies((enchantment, item) -> true);
      operation.setEnchantMaxLevel(enchantment -> Short.MAX_VALUE);
      operation.setEnchantsConflict((enchantment1, enchantment2) -> true);
      var state = new ReadableResultState(operation, anvil);

      assertThat("Combination must apply", function.canApply(operation, state));

      AnvilFunctionResult result = function.getResult(operation, state);
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

      var anvil = getMockInventory(base, addition);
      var operation = new AnvilOperation();
      operation.setItemsCombineEnchants(((itemStack, itemStack2) -> true));
      operation.setEnchantApplies((enchantment, item) -> true);
      operation.setEnchantMaxLevel(enchantment -> Short.MAX_VALUE);
      operation.setEnchantsConflict((enchantment1, enchantment2) -> false);
      var state = new ReadableResultState(operation, anvil);

      assertThat("Combination must apply", function.canApply(operation, state));

      AnvilFunctionResult result = function.getResult(operation, state);
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

      var anvil = getMockInventory(base, addition);
      var operation = new AnvilOperation();
      operation.setItemsCombineEnchants(((itemStack, itemStack2) -> true));
      operation.setEnchantApplies((enchantment, item) -> true);
      operation.setEnchantMaxLevel(enchantment -> Short.MAX_VALUE);
      operation.setEnchantsConflict((enchantment1, enchantment2) -> false);
      var state = new ReadableResultState(operation, anvil);

      assertThat("Combination must apply", function.canApply(operation, state));

      AnvilFunctionResult result = function.getResult(operation, state);
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
      super(AnvilFunction.COMBINE_ENCHANTMENTS_JAVA_EDITION);
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

      var anvil = getMockInventory(base, addition);
      var operation = new AnvilOperation();
      operation.setItemsCombineEnchants(((itemStack, itemStack2) -> true));
      operation.setEnchantApplies((enchantment, item) -> true);
      operation.setEnchantMaxLevel(enchantment -> Short.MAX_VALUE);
      operation.setEnchantsConflict((enchantment1, enchantment2) -> false);
      var state = new ReadableResultState(operation, anvil);

      assertThat("Combination must apply", function.canApply(operation, state));

      AnvilFunctionResult result = function.getResult(operation, state);
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
      super(AnvilFunction.COMBINE_ENCHANTMENTS_BEDROCK_EDITION);
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

      var anvil = getMockInventory(base, addition);
      var operation = new AnvilOperation();
      operation.setItemsCombineEnchants(((itemStack, itemStack2) -> true));
      operation.setEnchantApplies((enchantment, item) -> true);
      operation.setEnchantMaxLevel(enchantment -> Short.MAX_VALUE);
      operation.setEnchantsConflict((enchantment1, enchantment2) -> false);
      var state = new ReadableResultState(operation, anvil);

      assertThat("Combination must apply", function.canApply(operation, state));

      AnvilFunctionResult result = function.getResult(operation, state);
      assertThat("Material cost is unchanged", result.getMaterialCostIncrease(), is(0));
      result.modifyResult(state.getResult().getMeta());

      assertThat("Enchantments must be the same in result",
          EnchantmentUtil.getEnchants(state.getResult().getMeta()).entrySet(),
          both(everyItem(is(in(enchantments.entrySet())))).and(
              containsInAnyOrder(enchantments.entrySet().toArray())));
      assertThat("Cost must be unchanged", result.getLevelCostIncrease(), is(0));
    }

  }

  private static @NotNull AnvilInventory getMockInventory(
      @Nullable ItemStack base,
      @Nullable ItemStack addition) {
    var anvil = InventoryMocks.newAnvilMock();
    anvil.setItem(0, base);
    anvil.setItem(1, addition);

    return anvil;
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
