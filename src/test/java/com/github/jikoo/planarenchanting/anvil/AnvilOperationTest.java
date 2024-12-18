package com.github.jikoo.planarenchanting.anvil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.jikoo.planarenchanting.util.mock.ServerMocks;
import com.github.jikoo.planarenchanting.util.mock.enchantments.EnchantmentMocks;
import com.github.jikoo.planarenchanting.util.mock.inventory.InventoryMocks;
import com.github.jikoo.planarenchanting.util.mock.inventory.ItemFactoryMocks;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Server;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.AnvilView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
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
  public static Enchantment toolEnchantment;

  private AnvilOperation operation;

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

    toolEnchantment = Enchantment.EFFICIENCY;
    tag = Tag.ITEMS_ENCHANTABLE_MINING;
    doReturn(Set.of(TOOL)).when(tag).getValues();
  }

  @BeforeEach
  void beforeEach() {
    operation = new AnvilOperation();
  }

  @Test
  void testEnchantmentTarget() {
    var item = new ItemStack(TOOL);
    assertThat("Enchantment applies to tools", operation.enchantApplies(toolEnchantment, item));
    item.setType(INCOMPATIBLE);
    assertThat(
        "Enchantment does not apply to non-tools",
        operation.enchantApplies(toolEnchantment, item),
        is(false));
    operation.setEnchantApplies((enchant, itemStack) -> true);
    assertThat(
        "Enchantment applies with alternate predicate",
        operation.enchantApplies(toolEnchantment, item));
  }

  @Test
  void testEnchantmentConflict() {
    Enchantment conflict1 = Enchantment.SILK_TOUCH;
    Enchantment conflict2 = Enchantment.FORTUNE;

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

  private static @NotNull Stream<Enchantment> getEnchantments() {
    return Registry.ENCHANTMENT.stream();
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
    var anvil = getMockView(null, null);
    var result = operation.apply(anvil);
    assertThat("Result must be empty", result, is(AnvilResult.EMPTY));
  }

  @Test
  void testEmptyAdditionIsEmpty() {
    var anvil = getMockView(new ItemStack(TOOL), null);
    var result = operation.apply(anvil);
    assertThat("Result must be empty", result, is(AnvilResult.EMPTY));
  }

  @Test
  void testEmptyAdditionRenameNotEmpty() {
    var anvil = getMockView(new ItemStack(TOOL), null);
    when(anvil.getRenameText()).thenReturn("Sample Text");
    var result = operation.apply(anvil);
    assertThat("Result must not be empty", result, not(AnvilResult.EMPTY));
  }

  @Test
  void testMultipleBaseIsEmpty() {
    var base = new ItemStack(TOOL);
    base.setAmount(2);
    var addition = new ItemStack(TOOL);
    var anvil = getMockView(base, addition);
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

    var anvil = getMockView(base, addition);
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
    var anvil = getMockView(base, addition);
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

}
