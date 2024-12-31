package com.github.jikoo.planarenchanting.anvil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@DisplayName("Verify AnvilOperationState functionality")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AnvilStateTest {

  private AnvilBehavior behavior;
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
    behavior = AnvilBehavior.VANILLA;

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
    var state = new AnvilState(view);
    assertThat("Anvil must be provided instance", state.getAnvil(), is(view));
  }

  @Test
  void testBase() {
    var base = new ItemStack(Material.DIRT);
    view.setItem(0, base);
    var state = new AnvilState(view);
    assertThat("Base item must match", state.getBase().getItem(), is(base));
  }

  @Test
  void testAddition() {
    var addition = new ItemStack(Material.DIRT);
    view.setItem(1, addition);
    var state = new AnvilState(view);
    assertThat("Addition item must match", state.getAddition().getItem(), is(addition));
  }

  @Test
  void testGetSetLevelCost() {
    var state = new AnvilState(view);
    assertThat("Level cost starts at 0", state.getLevelCost(), is(0));
    int value = 10;
    state.setLevelCost(value);
    assertThat("Level cost is set", state.getLevelCost(), is(value));
  }

  @Test
  void testGetSetMaterialCost() {
    var state = new AnvilState(view);
    assertThat("Material cost starts at 0", state.getMaterialCost(), is(0));
    int value = 10;
    state.setMaterialCost(value);
    assertThat("Material cost is set", state.getMaterialCost(), is(value));
  }

}