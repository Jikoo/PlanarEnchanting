package com.github.jikoo.planarenchanting.anvil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import com.github.jikoo.planarenchanting.util.mock.ServerMocks;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.Tag;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Set;

class RepairMaterialFallthroughTest {

  @BeforeAll
  static void setUpAll() throws ClassNotFoundException {
    ServerMocks.mockServer();

    // Set up tag for fallthrough.
    Tag<ItemType> tag = Registry.ITEM.getTag(ItemTypeTagKeys.DIAMOND_TOOL_MATERIALS);
    doReturn(Set.of(TypedKey.create(RegistryKey.ITEM, ItemType.DIAMOND.getKey()))).when(tag).values();

    // Set up error for Repairable check.
    var componentType = DataComponentTypes.REPAIRABLE;
    ItemType type = ItemType.DIAMOND_PICKAXE;
    doThrow(IncompatibleClassChangeError.class).when(type).getDefaultData(componentType);

    // Touch RepairMaterial to initialize.
    Class.forName("com.github.jikoo.planarenchanting.anvil.RepairMaterial");
  }

  @Test
  void repairs() {
    assertThat(
        "Item is repairable",
        RepairMaterial.repairs(
            ItemType.DIAMOND_PICKAXE.createItemStack(),
            ItemType.DIAMOND.createItemStack()
        )
    );
  }

  @Test
  void repairsInvalid() {
    assertThat(
        "Item is not repairable with wrong material",
        RepairMaterial.repairs(
            ItemType.DIAMOND_PICKAXE.createItemStack(),
            ItemType.DIAMOND_PICKAXE.createItemStack()
        ),
        is(false)
    );
  }

  @Test
  void repairsNotRepairable() {
    assertThat(
        "Item is not repairable",
        RepairMaterial.repairs(
            ItemType.DIAMOND.createItemStack(),
            ItemType.DIAMOND_PICKAXE.createItemStack()
        ),
        is(false)
    );
  }

}
