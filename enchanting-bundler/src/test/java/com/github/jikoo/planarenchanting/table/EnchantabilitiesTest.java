package com.github.jikoo.planarenchanting.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;

@TestInstance(Lifecycle.PER_CLASS)
class EnchantabilitiesTest {

  private MockedStatic<Bukkit> bukkit;

  @BeforeAll
  void setUp() throws ClassNotFoundException {
    bukkit = mockStatic();
    // Set up registries.
    // Note that Registry.MATERIAL is an enum-based faux registry and cannot
    // be mocked effectively without tricks not available through Mockito alone
    // because it is initialized in place rather than fetched from the server.
    bukkit.when(() -> Bukkit.getRegistry(any())).thenAnswer(ignored -> mock(Registry.class));
    // Touch Registry to initialize static fields.
    Class.forName("org.bukkit.Registry");

    // Mock values for Registry.ITEM to allow Material#isItem on constants.
    doAnswer(invocation -> {
      NamespacedKey key = invocation.getArgument(0);
      if (key.getKey().equals("AIR")) {
        return null;
      }
      return mock(ItemType.class);
    }).when(Registry.ITEM).get(any());
  }

  @AfterAll
  void tearDown() {
    bukkit.close();
  }

  @ParameterizedTest
  @CsvSource({ "DIAMOND_PICKAXE,true", "AIR,false" })
  void ofMaterial(Material material, boolean isEnchantable) {
    assertThat(
        "Enchantability matches expectation",
        Enchantabilities.of(material),
        is(isEnchantable ? not(nullValue()) : nullValue())
    );
  }

  @ParameterizedTest
  @CsvSource({ "diamond_pickaxe,true", "air,false" })
  void ofItemType(String key, boolean isEnchantable) {
    NamespacedKey namespacedKey = NamespacedKey.minecraft(key);
    ItemType itemType = mock(ItemType.class);
    doReturn(namespacedKey).when(itemType).getKey();

    assertThat(
        "Enchantability matches expectation",
        Enchantabilities.of(itemType),
        is(isEnchantable ? not(nullValue()) : nullValue())
    );
  }

  @ParameterizedTest
  @CsvSource({ "DIAMOND_PICKAXE,true", "AIR,false" })
  void ofItemStack(Material material, boolean isEnchantable) {
    ItemStack itemStack = mock();
    doReturn(material).when(itemStack).getType();

    assertThat(
        "Enchantability matches expectation",
        Enchantabilities.of(itemStack),
        is(isEnchantable ? not(nullValue()) : nullValue())
    );
  }

}
