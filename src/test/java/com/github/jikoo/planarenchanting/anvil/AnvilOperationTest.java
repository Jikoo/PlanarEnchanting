package com.github.jikoo.planarenchanting.anvil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.enchantments.EnchantmentMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.github.jikoo.planarenchanting.util.EnchantmentHelper;
import com.github.jikoo.planarenchanting.util.mock.AnvilInventoryMock;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import com.github.jikoo.planarenchanting.util.mock.MockHelper;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/*
 * Note: These tests are only supposed to cover the functionality of the AnvilOperation class.
 * Specific operations are not verified, that is handled in more specific and thorough tests.
 */
@DisplayName("Verify AnvilOperation application")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AnvilOperationTest {

  private static final Material TOOL = Material.DIAMOND_SHOVEL;
  private static final Material TOOL_REPAIR = Material.DIAMOND;
  public static final Material BOOK = Material.ENCHANTED_BOOK;
  private static final Material INCOMPATIBLE = Material.STONE;
  public static final Enchantment TOOL_ENCHANTMENT = Enchantment.DIG_SPEED;

  private AnvilOperation operation;

  @BeforeAll
  void beforeAll() {
    MockBukkit.mock();
    EnchantmentHelper.setupToolEnchants();
  }

  @AfterAll
  void afterAll() {
    MockHelper.unmock();
  }

  @BeforeEach
  void beforeEach() {
    operation = new AnvilOperation();
  }

  @Test
  void testEnchantmentTarget() {
    var item = new ItemStack(TOOL);
    assertThat("Enchantment applies to tools", operation.enchantApplies(TOOL_ENCHANTMENT, item));
    item.setType(INCOMPATIBLE);
    assertThat(
        "Enchantment does not apply to non-tools",
        operation.enchantApplies(TOOL_ENCHANTMENT, item),
        is(false));
    operation.setEnchantApplies((enchant, itemStack) -> true);
    assertThat(
        "Enchantment applies with alternate predicate",
        operation.enchantApplies(TOOL_ENCHANTMENT, item));
  }

  @Test
  void testEnchantmentConflict() {
    Enchantment conflict1 = new EnchantmentMock(
        Enchantment.SILK_TOUCH.getKey(),
        Enchantment.SILK_TOUCH.getKey().getKey()) {
      @Override
      public boolean conflictsWith(@NotNull Enchantment other) {
        return other.getKey().equals(Enchantment.LOOT_BONUS_BLOCKS.getKey());
      }
    };

    Enchantment conflict2 = new EnchantmentMock(
        Enchantment.LOOT_BONUS_BLOCKS.getKey(),
        Enchantment.LOOT_BONUS_BLOCKS.getKey().getKey()) {
      @Override
      public boolean conflictsWith(@NotNull Enchantment other) {
        return other.getKey().equals(Enchantment.SILK_TOUCH.getKey());
      }
    };

    assertThat(
        "Vanilla enchantments conflict",
        operation.enchantsConflict(conflict1, conflict2));
    operation.setEnchantsConflict(((enchantment, enchantment2) -> false));
    assertThat(
        "Enchantments do not conflict with alternate predicate",
        operation.enchantsConflict(conflict1, conflict2),
        is(false));
  }

  @ParameterizedTest
  @MethodSource("getEnchantments")
  void testEnchantmentMaxLevel(Enchantment enchantment) {
    assertThat(
        "Enchantment max level must be vanilla",
        operation.getEnchantMaxLevel(enchantment),
        is(enchantment.getMaxLevel()));
    operation.setEnchantMaxLevel(enchant -> Short.MAX_VALUE);
    assertThat(
        "Enchantment max level must set as expected",
        operation.getEnchantMaxLevel(enchantment),
        is((int) Short.MAX_VALUE));
  }

  private static Stream<Arguments> getEnchantments() {
    return Arrays.stream(Enchantment.values()).map(Arguments::of);
  }

  @Test
  void testSameMaterialEnchantCombination() {
    ItemStack base = new ItemStack(TOOL);
    ItemStack addition = new ItemStack(TOOL);
    assertThat(
        "Same type combine enchantments",
        operation.itemsCombineEnchants(base, addition));
  }

  @Test
  void testEnchantedBookEnchantCombination() {
    ItemStack base = new ItemStack(TOOL);
    ItemStack addition = new ItemStack(BOOK);
    assertThat(
        "Enchanted books combine enchantments",
        operation.itemsCombineEnchants(base, addition));
  }

  @Test
  void testDifferentMaterialEnchantCombination() {
    ItemStack base = new ItemStack(TOOL);
    ItemStack addition = new ItemStack(INCOMPATIBLE);
    assertThat(
        "Incompatible materials do not combine enchantments",
        operation.itemsCombineEnchants(base, addition),
        not(true));
    operation.setItemsCombineEnchants((itemStack, itemStack2) -> true);
    assertThat(
        "Enchantments combine with alternate predicate",
        operation.itemsCombineEnchants(base, addition));
  }

  @Test
  void testEmptyBaseIsEmpty() {
    var anvil = getMockInventory(null, null);
    var result = operation.apply(anvil);
    assertThat("Result must be empty", result, is(AnvilResult.EMPTY));
  }

  @Test
  void testEmptyAdditionIsEmpty() {
    var anvil = getMockInventory(new ItemStack(TOOL), null);
    var result = operation.apply(anvil);
    assertThat("Result must be empty", result, is(AnvilResult.EMPTY));
  }

  @Test
  void testEmptyAdditionRenameNotEmpty() {
    var anvil = getMockInventory(new ItemStack(TOOL), null);
    anvil.setRenameText("Sample Text");
    var result = operation.apply(anvil);
    assertThat("Result must not be empty", result, not(AnvilResult.EMPTY));
  }

  @Test
  void testMultipleBaseIsEmpty() {
    var base = new ItemStack(TOOL);
    base.setAmount(2);
    var addition = new ItemStack(TOOL);
    var anvil = getMockInventory(base, addition);
    var result = operation.apply(anvil);
    assertThat("Result must be empty", result, is(AnvilResult.EMPTY));
  }

  @Test
  void testRepairWithMaterial() {
    var base = new ItemStack(TOOL);
    var itemMeta = base.getItemMeta();

    assertThat("ItemMeta must be Damageable", itemMeta, instanceOf(Damageable.class));
    assertThat("Material must have durability", (int) TOOL.getMaxDurability(), greaterThan(0));

    Damageable damageable = (Damageable) itemMeta;
    Objects.requireNonNull(damageable).setDamage(TOOL.getMaxDurability() - 1);
    base.setItemMeta(damageable);

    var addition = new ItemStack(TOOL_REPAIR);

    assertThat("Base must be repairable by addition", operation.itemRepairedBy(base, addition));

    var anvil = getMockInventory(base, addition);
    var result = operation.apply(anvil);

    assertThat("Result must not be empty", result, is(not(AnvilResult.EMPTY)));

    ItemStack resultItem = result.item();
    assertThat("Result must be of original type", resultItem.getType(), is(base.getType()));
    ItemMeta resultMeta = resultItem.getItemMeta();
    assertThat("Result must be Damageable", resultMeta, instanceOf(Damageable.class));
    assertThat(
        "Result must be repaired",
        ((Damageable) Objects.requireNonNull(resultMeta)).getDamage(),
        lessThan(damageable.getDamage()));
  }

  @Test
  void testRepairWithCombination() {
    var base = new ItemStack(TOOL);
    var itemMeta = base.getItemMeta();

    assertThat("ItemMeta must be Damageable", itemMeta, instanceOf(Damageable.class));
    assertThat("Material must have durability", (int) TOOL.getMaxDurability(), greaterThan(0));

    Damageable damageable = (Damageable) itemMeta;
    Objects.requireNonNull(damageable).setDamage(TOOL.getMaxDurability() - 1);
    base.setItemMeta(damageable);

    var addition = base.clone();
    var anvil = getMockInventory(base, addition);
    var result = operation.apply(anvil);

    assertThat("Result must not be empty", result, is(not(AnvilResult.EMPTY)));

    ItemStack resultItem = result.item();
    assertThat("Result must be of original type", resultItem.getType(), is(base.getType()));
    ItemMeta resultMeta = resultItem.getItemMeta();
    assertThat("Result must be Damageable", resultMeta, instanceOf(Damageable.class));
    assertThat(
        "Result must be repaired",
        ((Damageable) Objects.requireNonNull(resultMeta)).getDamage(),
        lessThan(damageable.getDamage()));
  }

  private static @NotNull AnvilInventoryMock getMockInventory(
      @Nullable ItemStack base,
      @Nullable ItemStack addition) {
    var anvil = new AnvilInventoryMock(new PlayerMock(MockBukkit.getMock(), "player1"));
    anvil.setItem(0, base);
    anvil.setItem(1, addition);

    return anvil;
  }

}
