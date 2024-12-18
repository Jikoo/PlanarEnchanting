package com.github.jikoo.planarenchanting.anvil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.jikoo.planarenchanting.anvil.mock.ReadableResultState;
import com.github.jikoo.planarenchanting.util.MetaCachedStack;
import com.github.jikoo.planarenchanting.util.mock.ServerMocks;
import com.github.jikoo.planarenchanting.util.mock.enchantments.EnchantmentMocks;
import com.github.jikoo.planarenchanting.util.mock.inventory.InventoryMocks;
import com.github.jikoo.planarenchanting.util.mock.inventory.ItemFactoryMocks;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.view.AnvilView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@DisplayName("Verify AnvilOperationState functionality")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AnvilOperationStateTest {

  private AnvilOperation operation;
  private AnvilView view;

  @BeforeAll
  void beforeAll() {
    Server server = ServerMocks.mockServer();

    ItemFactory factory = ItemFactoryMocks.mockFactory();
    when(server.getItemFactory()).thenReturn(factory);

    // RepairMaterial requires these tags to be set up.
    Tag<Material> tag = Tag.ITEMS_STONE_TOOL_MATERIALS;
    doReturn(Set.of(Material.STONE, Material.ANDESITE, Material.GRANITE, Material.DIORITE))
        .when(tag).getValues();
    tag = Tag.PLANKS;
    doReturn(Set.of(Material.ACACIA_PLANKS, Material.BIRCH_PLANKS, Material.OAK_PLANKS)) //etc. non-exhaustive list
        .when(tag).getValues();

    EnchantmentMocks.init();
  }

  @BeforeEach
  void beforeEach() {
    operation = new AnvilOperation();

    var anvil = InventoryMocks.newAnvilMock();
    view = mock(AnvilView.class);
    doAnswer(params -> anvil.getItem(params.getArgument(0)))
        .when(view).getItem(anyInt());
    doAnswer(params -> {
      anvil.setItem(params.getArgument(0), params.getArgument(1));
      return null;
    }).when(view).setItem(anyInt(), any());
  }

  @Test
  void testGetAnvil() {
    var state = new AnvilOperationState(operation, view);
    assertThat("Anvil must be provided instance", state.getAnvil(), is(view));
  }

  @Test
  void testBase() {
    var base = new ItemStack(Material.DIRT);
    view.setItem(0, base);
    var state = new AnvilOperationState(operation, view);
    assertThat("Base item must match", state.getBase().getItem(), is(base));
  }

  @Test
  void testAddition() {
    var addition = new ItemStack(Material.DIRT);
    view.setItem(1, addition);
    var state = new AnvilOperationState(operation, view);
    assertThat("Addition item must match", state.getAddition().getItem(), is(addition));
  }

  @Test
  void testGetSetLevelCost() {
    var state = new AnvilOperationState(operation, view);
    assertThat("Level cost starts at 0", state.getLevelCost(), is(0));
    int value = 10;
    state.setLevelCost(value);
    assertThat("Level cost is set", state.getLevelCost(), is(value));
  }

  @Test
  void testGetSetMaterialCost() {
    var state = new AnvilOperationState(operation, view);
    assertThat("Material cost starts at 0", state.getMaterialCost(), is(0));
    int value = 10;
    state.setMaterialCost(value);
    assertThat("Material cost is set", state.getMaterialCost(), is(value));
  }

  @Test
  void testApplyNonApplicable() {
    var function = new AnvilFunction() {
      @Override
      public boolean canApply(
          @NotNull AnvilOperation operation,
          @NotNull AnvilOperationState state) {
        return false;
      }

      @Override
      public @NotNull AnvilFunctionResult getResult(
          @NotNull AnvilOperation operation,
          @NotNull AnvilOperationState state) {
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

    var state = new AnvilOperationState(operation, view);
    assertThat(
        "Non-applicable function does not apply",
        state.apply(function),
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
          @NotNull AnvilOperation operation,
          @NotNull AnvilOperationState state) {
        return true;
      }

      @Override
      public @NotNull AnvilFunctionResult getResult(
          @NotNull AnvilOperation operation,
          @NotNull AnvilOperationState state) {
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

    var state = new AnvilOperationState(operation, view);

    assertThat("Applicable function applies", state.apply(function));
    assertThat("Level cost is added", state.getLevelCost(), is(value));
    assertThat("Material cost is added", state.getMaterialCost(), is(value));

    state.apply(function);
    assertThat("Level cost is added again", state.getLevelCost(), is(value * 2));
    assertThat("Material cost is added again", state.getMaterialCost(), is(value * 2));
  }

  @Test
  void testForgeNullBaseMeta() {
    view.setItem(0, new ItemStack(Material.AIR) {
      @Override
      public @Nullable ItemMeta getItemMeta() {
        return null;
      }
    });

    var state = new AnvilOperationState(operation, view);

    assertThat("AnvilResult must be empty constant", state.forge(), is(AnvilResult.EMPTY));
  }

  @Test
  void testForgeNullResultMeta() {
    view.setItem(0, new ItemStack(Material.AIR) {
      @Override
      public @Nullable ItemMeta getItemMeta() {
        return null;
      }

      @Override
      public @NotNull ItemStack clone() {
        // Silence compiler warning.
        super.clone();
        // Return same instance when cloning - this makes the result have a null meta.
        // This is necessary because MockBukkit doesn't have the ability to create null metas
        // with its ItemFactory implementation.
        return this;
      }
    });

    var state = new AnvilOperationState(operation, view) {
      private final MetaCachedStack fakeBase = new MetaCachedStack(new ItemStack(Material.DIRT));
      @Override
      public @NotNull MetaCachedStack getBase() {
        return this.fakeBase;
      }
    };

    assertThat("AnvilResult must be empty constant", state.forge(), is(AnvilResult.EMPTY));
  }

  @Test
  void testForgeIgnoreRepairCost() {
    view.setItem(0, new ItemStack(Material.DIRT));

    var state = new ReadableResultState(operation, view);
    var meta = state.getResult().getMeta();

    assertThat("Meta must not be null", meta, notNullValue());
    assertThat("Meta must be Repairable", meta, instanceOf(Repairable.class));

    ((Repairable) meta).setRepairCost(100);

    assertThat("AnvilResult must be empty constant", state.forge(), is(AnvilResult.EMPTY));
  }

  @Test
  void testForgeIgnoreDisplayNameWithAddition() {
    view.setItem(0, new ItemStack(Material.DIRT));
    view.setItem(1, new ItemStack(Material.DIRT));

    var state = new ReadableResultState(operation, view);
    var meta = state.getResult().getMeta();

    assertThat("Meta must not be null", meta, notNullValue());

    meta.setDisplayName("Cool beans");

    assertThat("AnvilResult must be empty constant", state.forge(), is(AnvilResult.EMPTY));
  }

  @Test
  void testForge() {
    view.setItem(0, new ItemStack(Material.DIRT));

    var state = new ReadableResultState(operation, view);
    var meta = state.getResult().getMeta();

    assertThat("Meta must not be null", meta, notNullValue());

    meta.setDisplayName("Cool beans");

    assertThat(
        "AnvilResult must not be empty constant",
        state.forge(),
        is(not(AnvilResult.EMPTY)));
  }

}