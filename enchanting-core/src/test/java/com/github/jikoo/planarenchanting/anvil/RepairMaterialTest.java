package com.github.jikoo.planarenchanting.anvil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.github.jikoo.planarenchanting.util.mock.ServerMocks;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Repairable;
import io.papermc.paper.registry.set.RegistryKeySet;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RepairMaterialTest {

  @BeforeAll
  static void setUpAll() throws ClassNotFoundException {
    ServerMocks.mockServer();

    // Set up Repairable functionality before usage.
    var componentType = DataComponentTypes.REPAIRABLE;
    ItemType type = ItemType.DIAMOND_PICKAXE;
    Repairable repairable = mock();
    doReturn(repairable).when(type).getDefaultData(componentType);

    // Touch RepairMaterial to initialize.
    Class.forName("com.github.jikoo.planarenchanting.anvil.RepairMaterial");
  }

  @Test
  void repairs() {
    Repairable repairable = mock();
    RegistryKeySet<ItemType> keys = mock();
    doReturn(true).when(keys).contains(any());
    doReturn(keys).when(repairable).types();
    ItemStack item = ItemType.DIAMOND_PICKAXE.createItemStack();
    doReturn(repairable).when(item).getData(DataComponentTypes.REPAIRABLE);

    assertThat(
        "Item is repairable",
        RepairMaterial.repairs(item, ItemType.DIAMOND.createItemStack())
    );
  }

  @Test
  void repairsInvalid() {
    Repairable repairable = mock();
    RegistryKeySet<ItemType> keys = mock();
    doReturn(keys).when(repairable).types();
    ItemStack item = ItemType.DIAMOND_PICKAXE.createItemStack();
    doReturn(repairable).when(item).getData(DataComponentTypes.REPAIRABLE);

    assertThat(
        "Item is not repairable",
        RepairMaterial.repairs(item, ItemType.DIAMOND.createItemStack()),
        is(false)
    );
  }

  @Test
  void repairsNotRepairable() {
    assertThat(
        "Item is not repairable",
        RepairMaterial.repairs(
            ItemType.DIAMOND_PICKAXE.createItemStack(),
            ItemType.DIAMOND.createItemStack()
        ),
        is(false)
    );
  }

}
