package com.github.jikoo.planarenchanting.anvil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MetaVanillaBehaviorTest {

  private MockedStatic<Bukkit> bukkit;
  private MockedStatic<RepairMaterial> repairMaterial;
  private MetaVanillaBehavior behavior;

  @BeforeAll
  void setUp() {
    bukkit = mockStatic();
    bukkit.when(() -> Bukkit.getRegistry(any())).thenAnswer(invocation -> mock(Registry.class));
    repairMaterial = mockStatic();
    repairMaterial.when(() -> RepairMaterial.repairs(any(), any())).thenReturn(true);
  }

  @AfterAll
  void tearDownAll() {
    repairMaterial.close();
    bukkit.close();
  }

  @BeforeEach
  void setUpEach() {
    behavior = new MetaVanillaBehavior();
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
    ItemStack baseStack = mock();
    doReturn(baseMat).when(baseStack).getType();
    MetaCachedStack base = mock();
    doReturn(baseStack).when(base).getItem();
    ItemStack additionStack = mock();
    doReturn(additionMat).when(additionStack).getType();
    MetaCachedStack addition = mock();
    doReturn(additionStack).when(addition).getItem();

    assertThat(
        "Like items and enchanted book additions combine enchantments",
        behavior.itemsCombineEnchants(base, addition),
        is(result)
    );
  }

  @Test
  void itemRepairedBy() {
    MetaCachedStack base = mock();
    MetaCachedStack addition = mock();

    assertThat(
        "Delegates to RepairMaterial",
        behavior.itemRepairedBy(base, addition),
        is(true)
    );
    repairMaterial.verify(() -> RepairMaterial.repairs(any(), any()));
  }

}