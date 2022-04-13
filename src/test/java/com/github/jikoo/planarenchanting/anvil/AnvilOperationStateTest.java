package com.github.jikoo.planarenchanting.anvil;

import static com.github.jikoo.planarenchanting.util.matcher.ItemMatcher.isItemEqual;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import be.seeseemelk.mockbukkit.MockBukkit;
import com.github.jikoo.planarenchanting.anvil.mock.ReadableResultState;
import com.github.jikoo.planarenchanting.util.MetaCachedStack;
import com.github.jikoo.planarenchanting.util.mock.AnvilInventoryMock;
import com.github.jikoo.planarenchanting.util.mock.MockHelper;
import org.bukkit.Material;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@DisplayName("Verify AnvilOperationState functionality")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AnvilOperationStateTest {

  private AnvilOperation operation;
  private AnvilInventory inventory;

  @BeforeAll
  void beforeAll() {
    MockBukkit.mock();
  }

  @AfterAll
  void afterAll() {
    MockHelper.unmock();
  }

  @BeforeEach
  void beforeEach() {
    operation = new AnvilOperation();
    inventory = new AnvilInventoryMock(null);
  }

  @Test
  void testGetAnvil() {
    var state = new AnvilOperationState(operation, inventory);
    assertThat("Anvil must be provided instance", state.getAnvil(), is(inventory));
  }

  @Test
  void testBase() {
    var base = new ItemStack(Material.DIRT);
    inventory.setItem(0, base);
    var state = new AnvilOperationState(operation, inventory);
    assertThat("Base item must match", state.getBase().getItem(), isItemEqual(base));
  }

  @Test
  void testAddition() {
    var addition = new ItemStack(Material.DIRT);
    inventory.setItem(1, addition);
    var state = new AnvilOperationState(operation, inventory);
    assertThat("Addition item must match", state.getAddition().getItem(), isItemEqual(addition));
  }

  @Test
  void testGetSetLevelCost() {
    var state = new AnvilOperationState(operation, inventory);
    assertThat("Level cost starts at 0", state.getLevelCost(), is(0));
    int value = 10;
    state.setLevelCost(value);
    assertThat("Level cost is set", state.getLevelCost(), is(value));
  }

  @Test
  void testGetSetMaterialCost() {
    var state = new AnvilOperationState(operation, inventory);
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

    var state = new AnvilOperationState(operation, inventory);
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

    var state = new AnvilOperationState(operation, inventory);

    assertThat("Applicable function applies", state.apply(function));
    assertThat("Level cost is added", state.getLevelCost(), is(value));
    assertThat("Material cost is added", state.getMaterialCost(), is(value));

    state.apply(function);
    assertThat("Level cost is added again", state.getLevelCost(), is(value * 2));
    assertThat("Material cost is added again", state.getMaterialCost(), is(value * 2));
  }

  @Test
  void testForgeNullBaseMeta() {
    inventory.setItem(0, new ItemStack(Material.AIR) {
      @Override
      public @Nullable ItemMeta getItemMeta() {
        return null;
      }
    });

    var state = new AnvilOperationState(operation, inventory);

    assertThat("AnvilResult must be empty constant", state.forge(), is(AnvilResult.EMPTY));
  }

  @Test
  void testForgeNullResultMeta() {
    inventory.setItem(0, new ItemStack(Material.AIR) {
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

    var state = new AnvilOperationState(operation, inventory) {
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
    inventory.setItem(0, new ItemStack(Material.DIRT));

    var state = new ReadableResultState(operation, inventory);
    var meta = state.getResult().getMeta();

    assertThat("Meta must not be null", meta, notNullValue());
    assertThat("Meta must be Repairable", meta, instanceOf(Repairable.class));

    ((Repairable) meta).setRepairCost(100);

    assertThat("AnvilResult must be empty constant", state.forge(), is(AnvilResult.EMPTY));
  }

  @Test
  void testForgeIgnoreDisplayNameWithAddition() {
    inventory.setItem(0, new ItemStack(Material.DIRT));
    inventory.setItem(1, new ItemStack(Material.DIRT));

    var state = new ReadableResultState(operation, inventory);
    var meta = state.getResult().getMeta();

    assertThat("Meta must not be null", meta, notNullValue());

    meta.setDisplayName("Cool beans");

    assertThat("AnvilResult must be empty constant", state.forge(), is(AnvilResult.EMPTY));
  }

  @Test
  void testForge() {
    inventory.setItem(0, new ItemStack(Material.DIRT));

    var state = new ReadableResultState(operation, inventory);
    var meta = state.getResult().getMeta();

    assertThat("Meta must not be null", meta, notNullValue());

    meta.setDisplayName("Cool beans");

    assertThat(
        "AnvilResult must not be empty constant",
        state.forge(),
        is(not(AnvilResult.EMPTY)));
  }

}