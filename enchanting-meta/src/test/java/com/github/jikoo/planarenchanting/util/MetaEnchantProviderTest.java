package com.github.jikoo.planarenchanting.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
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
import org.bukkit.enchantments.Enchantment;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MetaEnchantProviderTest {

  private MockedStatic<Bukkit> bukkit;
  private MetaEnchantProvider provider;

  @BeforeAll
  void setUp() {
    bukkit = mockStatic();
    bukkit.when(() -> Bukkit.getRegistry(any())).thenAnswer(invocation -> {
      Registry<?> registry = mock(Registry.class);
      if (Enchantment.class.isAssignableFrom(invocation.getArgument(0))) {
        Answer<Enchantment> getEnchant = invGet -> {
          NamespacedKey key = invGet.getArgument(0);
          if (key.getKey().equals("unbreaking")) {
            // oops, all breaking
            return null;
          }
          Enchantment enchant = mock();
          doReturn(key).when(enchant).getKey();
          return enchant;
        };
        doAnswer(getEnchant).when(registry).getOrThrow(any());
        doAnswer(getEnchant).when(registry).get(any());
      }
      return registry;
    });

    provider = new MetaEnchantProvider();
  }

  @AfterAll
  void tearDown() {
    bukkit.close();
  }

  @Test
  void of() {
    EnchantData enchantData = provider.of(Enchantment.SILK_TOUCH);

    assertThat("Enchantment is supported", enchantData, is(notNullValue()));
    assertThat("Weight is not default", enchantData.getWeight(), is(not(5)));
    assertThat("Trident enchant uses enchant definition", enchantData.isTridentEnchant(), is(false));
  }

  @ParameterizedTest
  @CsvSource({ "true,true", "true,false", "false,true", "false,false" })
  void ofUnknown(boolean isTrident, boolean isTool) {
    Enchantment enchantment = mock(Enchantment.class);
    doReturn(NamespacedKey.minecraft("trident_" + isTrident + "_" + isTool)).when(enchantment).getKey();

    doAnswer(invocation -> {
      if (isTool) return true;
      ItemStack stack = invocation.getArgument(0);
      Material mat = stack.getType();
      return isTrident && mat == Material.TRIDENT;
    }).when(enchantment).canEnchantItem(any());

    EnchantData data = provider.of(enchantment);

    assertThat("Enchantment is supported", data, is(notNullValue()));
    assertThat("Weight is default", data.getWeight(), is(5));
    assertThat("Anvil cost is default", data.getAnvilCost(), is(2));
    assertThat("Min cost uses unbreaking formula", data.getMinModifiedCost(1), is(5));
    assertThat("Min cost uses unbreaking formula", data.getMinModifiedCost(2), is(13));
    assertThat("Max cost uses unbreaking formula", data.getMaxModifiedCost(1), is(55));
    assertThat("Max cost uses unbreaking formula", data.getMaxModifiedCost(2), is(63));
    assertThat("Trident enchant uses enchant definition", data.isTridentEnchant(), is(isTrident && !isTool));
  }

}
