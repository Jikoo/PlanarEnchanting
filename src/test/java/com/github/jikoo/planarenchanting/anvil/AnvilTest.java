package com.github.jikoo.planarenchanting.anvil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.jikoo.planarenchanting.util.MetaCachedStack;
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
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.view.AnvilView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/*
 * Note: These tests are only supposed to cover the functionality of the AnvilOperation class.
 * Specific operations are not verified, that is handled in more specific and thorough tests.
 */
@DisplayName("Verify Anvil application")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AnvilTest {

  private static final Material TOOL = Material.DIAMOND_SHOVEL;
  private static final Material TOOL_REPAIR = Material.DIAMOND;
  public static final Material BOOK = Material.ENCHANTED_BOOK;
  private static final Material INCOMPATIBLE = Material.STONE;
  public static Enchantment toolEnchantment;

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

  @Test
  void testApplyNonApplicable() {
    var function = new AnvilFunction() {
      @Override
      public boolean canApply(
          @NotNull AnvilBehavior behavior,
          @NotNull AnvilState state) {
        return false;
      }

      @Override
      public @NotNull AnvilFunctionResult getResult(
          @NotNull AnvilBehavior behavior,
          @NotNull AnvilState state) {
        return new AnvilFunctionResult() {
          @Override
          public int getLevelCostIncrease() {
            return 10;
          }

          @Override
          public int getMaterialCostIncrease() {
            return 10;
          }
        };
      }
    };

    var anvil = new Anvil();
    var state = new AnvilState(getMockView(null, null));
    assertThat(
        "Non-applicable function does not apply",
        anvil.apply(state, function),
        is(false));
    assertThat("Level cost is unchanged", state.getLevelCost(), is(0));
    assertThat("Material cost is unchanged", state.getMaterialCost(), is(0));
  }

  @Test
  void testApply() {
    final int value = 10;
    var function = new AnvilFunction() {
      @Override
      public boolean canApply(
          @NotNull AnvilBehavior behavior,
          @NotNull AnvilState state) {
        return true;
      }

      @Override
      public @NotNull AnvilFunctionResult getResult(
          @NotNull AnvilBehavior behavior,
          @NotNull AnvilState state) {
        return new AnvilFunctionResult() {
          @Override
          public int getLevelCostIncrease() {
            return value;
          }

          @Override
          public int getMaterialCostIncrease() {
            return value;
          }
        };
      }
    };

    var anvil = new Anvil();
    var state = new AnvilState(getMockView(null, null));

    assertThat("Applicable function applies", anvil.apply(state, function));
    assertThat("Level cost is added", state.getLevelCost(), is(value));
    assertThat("Material cost is added", state.getMaterialCost(), is(value));

    anvil.apply(state, function);
    assertThat("Level cost is added again", state.getLevelCost(), is(value * 2));
    assertThat("Material cost is added again", state.getMaterialCost(), is(value * 2));
  }

  @Test
  void testForgeNullBaseMeta() {
    var anvil = new Anvil();
    var state = new AnvilState(
        getMockView(new ItemStack(Material.AIR) {
          @Override
          public @Nullable ItemMeta getItemMeta() {
            return null;
          }
        }, null));

    assertThat("AnvilResult must be empty constant", anvil.forge(state), is(AnvilResult.EMPTY));
  }

  @Test
  void testForgeNullResultMeta() {
    var state = new AnvilState(
        getMockView(new ItemStack(Material.AIR) {
          @Override
          public @Nullable ItemMeta getItemMeta() {
            return null;
          }

          @Override
          public @NotNull ItemStack clone() {
            // Silence compiler warning.
            super.clone();
            // Return same instance when cloning - this makes the result have a null meta.
            return this;
          }
        }, null)) {
      private final MetaCachedStack fakeBase = new MetaCachedStack(new ItemStack(Material.DIRT));
      @Override
      public @NotNull MetaCachedStack getBase() {
        return this.fakeBase;
      }
    };
    var anvil = new Anvil();

    assertThat("AnvilResult must be empty constant", anvil.forge(state), is(AnvilResult.EMPTY));
  }

  @Test
  void testForgeIgnoreRepairCost() {
    var state = new AnvilState(getMockView(new ItemStack(Material.DIRT), null));
    var meta = state.result.getMeta();

    assertThat("Meta must not be null", meta, notNullValue());
    assertThat("Meta must be Repairable", meta, instanceOf(Repairable.class));

    ((Repairable) meta).setRepairCost(100);

    var anvil = new Anvil();
    assertThat("AnvilResult must be empty constant", anvil.forge(state), is(AnvilResult.EMPTY));
  }

  @Test
  void testForgeIgnoreDisplayNameWithAddition() {
    var state = new AnvilState(
        getMockView(new ItemStack(Material.DIRT), new ItemStack(Material.DIRT)));
    var meta = state.result.getMeta();

    assertThat("Meta must not be null", meta, notNullValue());

    meta.setDisplayName("Cool beans");

    var anvil = new Anvil();
    assertThat("AnvilResult must be empty constant", anvil.forge(state), is(AnvilResult.EMPTY));
  }

  @Test
  void testForge() {
    var state = new AnvilState(getMockView(new ItemStack(Material.DIRT), null));
    var meta = state.result.getMeta();

    assertThat("Meta must not be null", meta, notNullValue());

    meta.setDisplayName("Cool beans");

    var anvil = new Anvil();
    assertThat(
        "AnvilResult must not be empty constant",
        anvil.forge(state),
        is(not(AnvilResult.EMPTY)));
  }

  @Test
  void testEnchantmentTarget() {
    var item = new MetaCachedStack(new ItemStack(TOOL));
    assertThat(
        "Enchantment applies to tools",
        AnvilBehavior.VANILLA.enchantApplies(toolEnchantment, item));
    item.getItem().setType(INCOMPATIBLE);
    assertThat(
        "Enchantment does not apply to non-tools",
        AnvilBehavior.VANILLA.enchantApplies(toolEnchantment, item),
        is(false));
  }

  @Test
  void testEnchantmentConflict() {
    assertThat(
        "Vanilla enchantments conflict",
        AnvilBehavior.VANILLA.enchantsConflict(Enchantment.SILK_TOUCH, Enchantment.FORTUNE));
    assertThat(
        "Nonconflicting enchantments do not conflict",
        AnvilBehavior.VANILLA.enchantsConflict(Enchantment.EFFICIENCY, Enchantment.FORTUNE),
        is(false));
  }

  @ParameterizedTest
  @MethodSource("getEnchantments")
  void testEnchantmentMaxLevel(Enchantment enchantment) {
    assertThat(
        "Enchantment max level must be vanilla",
        AnvilBehavior.VANILLA.getEnchantMaxLevel(enchantment),
        is(enchantment.getMaxLevel()));
  }

  private static @NotNull Stream<Enchantment> getEnchantments() {
    return Registry.ENCHANTMENT.stream();
  }

  @Test
  void testSameMaterialEnchantCombination() {
    var base = new MetaCachedStack(new ItemStack(TOOL));
    var addition = new MetaCachedStack(new ItemStack(TOOL));
    assertThat(
        "Same type combine enchantments",
        AnvilBehavior.VANILLA.itemsCombineEnchants(base, addition));
  }

  @Test
  void testEnchantedBookEnchantCombination() {
    var base = new MetaCachedStack(new ItemStack(TOOL));
    var addition = new MetaCachedStack(new ItemStack(BOOK));
    assertThat(
        "Enchanted books combine enchantments",
        AnvilBehavior.VANILLA.itemsCombineEnchants(base, addition));
  }

  @Test
  void testDifferentMaterialEnchantCombination() {
    var base = new MetaCachedStack(new ItemStack(TOOL));
    var addition = new MetaCachedStack(new ItemStack(INCOMPATIBLE));
    assertThat(
        "Incompatible materials do not combine enchantments",
        AnvilBehavior.VANILLA.itemsCombineEnchants(base, addition),
        is(false));
  }

  @Test
  void testEmptyBaseIsEmpty() {
    var anvil = getMockView(null, null);
    var result = new Anvil().getResult(anvil);
    assertThat("Result must be empty", result, is(AnvilResult.EMPTY));
  }

  @Test
  void testEmptyAdditionIsEmpty() {
    var anvil = getMockView(new ItemStack(TOOL), null);
    var result = new Anvil().getResult(anvil);
    assertThat("Result must be empty", result, is(AnvilResult.EMPTY));
  }

  @Test
  void testEmptyAdditionRenameNotEmpty() {
    var anvil = getMockView(new ItemStack(TOOL), null);
    when(anvil.getRenameText()).thenReturn("Sample Text");
    var result = new Anvil().getResult(anvil);
    assertThat("Result must not be empty", result, not(AnvilResult.EMPTY));
  }

  @Test
  void testMultipleBaseIsEmpty() {
    var base = new ItemStack(TOOL);
    base.setAmount(2);
    var addition = new ItemStack(TOOL);
    var anvil = getMockView(base, addition);
    var result = new Anvil().getResult(anvil);
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

    assertThat("Base must be repairable by addition", AnvilBehavior.VANILLA.itemRepairedBy(new MetaCachedStack(base), new MetaCachedStack(addition)));

    var anvil = getMockView(base, addition);
    var result = new Anvil().getResult(anvil);

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
    var result = new Anvil().getResult(anvil);

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
