package com.github.jikoo.planarenchanting.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.github.jikoo.planarenchanting.util.mock.ServerMocks;
import org.bukkit.Material;
import org.bukkit.inventory.ItemType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EnchantabilityFallthroughTest {

  @BeforeAll
  static void setUpAll() throws ClassNotFoundException {
    ServerMocks.mockServer();

    // Set up error for item enchantability checks.
    ItemType type = ItemType.DIAMOND_PICKAXE;
    doThrow(IncompatibleClassChangeError.class).when(type).getDefaultData(any());

    // Touch Enchantability.
    Class.forName("com.github.jikoo.planarenchanting.table.Enchantability");
  }

  @Test
  void forMaterialEnchantable() {
    assertThat(
        "Diamond pickaxe is enchantable",
        Enchantability.forMaterial(Material.DIAMOND_PICKAXE),
        is(notNullValue())
    );
  }

  @Test
  void forMaterialUnenchantable() {
    assertThat(
        "Diamond is not enchantable",
        Enchantability.forMaterial(Material.DIAMOND),
        is(nullValue())
    );
  }

  @Test
  void forTypeEnchantable() {
    assertThat(
        "Diamond pickaxe is enchantable",
        Enchantability.forType(ItemType.DIAMOND_PICKAXE),
        is(notNullValue())
    );
  }

  @Test
  void forTypeUnenchantable() {
    assertThat(
        "Diamond is not enchantable",
        Enchantability.forType(ItemType.DIAMOND),
        is(nullValue())
    );
  }

  @Test
  void forItemEnchantable() {
    var item = ItemType.DIAMOND_PICKAXE.createItemStack();

    assertThat(
        "Diamond pickaxe is enchantable",
        Enchantability.forItem(item),
        is(notNullValue())
    );
  }

  @Test
  void forItemUnenchantable() {
    var item = ItemType.DIAMOND.createItemStack();

    assertThat(
        "Diamond is not enchantable",
        Enchantability.forItem(item),
        is(nullValue())
    );
  }

}
