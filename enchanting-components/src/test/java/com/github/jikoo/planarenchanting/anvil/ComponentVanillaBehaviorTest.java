package com.github.jikoo.planarenchanting.anvil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.github.jikoo.planarenchanting.util.mock.ServerMocks;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Repairable;
import io.papermc.paper.registry.set.RegistryKeySet;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ComponentVanillaBehaviorTest {

  private ComponentVanillaBehavior behavior;

  @BeforeAll
  void setUp() {
    ServerMocks.mockServer();
    // Touch to initialize and ensure mocking is complete.
    DataComponentTypes.REPAIR_COST.key();
  }

  @BeforeEach
  void setUpEach() {
    behavior = new ComponentVanillaBehavior();
  }

  @ParameterizedTest
  @ValueSource(booleans = { true, false })
  void enchantApplies(boolean applies) {
    Enchantment enchantment = mock();
    doReturn(applies).when(enchantment).canEnchantItem(any());
    assertThat(
        "Behavior applies if enchant applies",
        behavior.enchantApplies(enchantment, mock()),
        is(applies)
    );
    verify(enchantment).canEnchantItem(any());
    verifyNoMoreInteractions(enchantment);
  }

  @ParameterizedTest
  @CsvSource({
      "DIAMOND,DIAMOND,true",
      "DIAMOND_PICKAXE,DIAMOND,false",
      "DIAMOND,ENCHANTED_BOOK,true",
      "ENCHANTED_BOOK,ENCHANTED_BOOK,true",
      "ENCHANTED_BOOK,DIAMOND,false"
  })
  void itemsCombineEnchants(Material baseMat, Material additionMat, boolean result) {
    ItemStack base = mock();
    doReturn(baseMat).when(base).getType();
    ItemStack addition = mock();
    doReturn(additionMat).when(addition).getType();

    assertThat(
        "Like items and enchanted book additions combine enchantments",
        behavior.itemsCombineEnchants(base, addition),
        is(result)
    );
  }

  @Test
  void itemRepairedByNotRepairable() {
    ItemStack base = mock();
    ItemStack addition = mock();

    assertThat(
        "Item without Repairable is not repairable",
        behavior.itemRepairedBy(base, addition),
        is(false)
    );
  }

  @ParameterizedTest
  @ValueSource(booleans = { true, false })
  void itemRepairedBy(boolean repairedBy) {
    RegistryKeySet<ItemType> types = mock();
    doReturn(repairedBy).when(types).contains(any());
    Repairable repairable = mock();
    doReturn(types).when(repairable).types();
    ItemStack base = mock();
    doReturn(repairable).when(base).getData(DataComponentTypes.REPAIRABLE);
    ItemStack addition = mock();
    doReturn(Material.DIRT).when(addition).getType();

    assertThat(
        "Item is repairable as expected",
        behavior.itemRepairedBy(base, addition),
        is(repairedBy)
    );
  }

}
