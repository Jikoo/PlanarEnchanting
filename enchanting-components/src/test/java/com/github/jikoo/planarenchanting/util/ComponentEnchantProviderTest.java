package com.github.jikoo.planarenchanting.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.github.jikoo.planarenchanting.util.mock.ServerMocks;
import io.papermc.paper.registry.keys.ItemTypeKeys;
import io.papermc.paper.registry.set.RegistryKeySet;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ComponentEnchantProviderTest {

  private ComponentEnchantProvider provider;
  private Enchantment enchant;

  @BeforeAll
  void setUpAll() {
    ServerMocks.mockServer();
    provider = new ComponentEnchantProvider();
  }

  @BeforeEach
  void setUpEach() {
    enchant = mock();
  }

  @ParameterizedTest
  @ValueSource(ints = { 1, 2, 5, 10 })
  void getWeight(int weight) {
    doReturn(weight).when(enchant).getWeight();

    EnchantData data = provider.of(enchant);

    assertThat("Weight is fetched from enchantment", data.getWeight(), is(weight));
    verify(enchant).getWeight();
    verifyNoMoreInteractions(enchant);
  }

  @Test
  void getAnvilCost() {
    int cost = 5;
    doReturn(cost).when(enchant).getAnvilCost();

    EnchantData data = provider.of(enchant);

    assertThat("Anvil cost is fetched from enchantment", data.getAnvilCost(), is(cost));
    verify(enchant).getAnvilCost();
    verifyNoMoreInteractions(enchant);
  }

  @Test
  void getMinModifiedCost() {
    int cost = 5;
    doReturn(cost).when(enchant).getMinModifiedCost(anyInt());

    EnchantData data = provider.of(enchant);

    int level = 1;
    assertThat("Min cost is fetched from enchantment", data.getMinModifiedCost(level), is(cost));
    verify(enchant).getMinModifiedCost(level);
    verifyNoMoreInteractions(enchant);
  }

  @Test
  void getMaxModifiedCost() {
    int cost = 5;
    doReturn(cost).when(enchant).getMaxModifiedCost(anyInt());

    EnchantData data = provider.of(enchant);

    int level = 1;
    assertThat("Max cost is fetched from enchantment", data.getMaxModifiedCost(level), is(cost));
    verify(enchant).getMaxModifiedCost(level);
    verifyNoMoreInteractions(enchant);
  }

  @ParameterizedTest
  @ValueSource(booleans = { true, false })
  void isTridentEnchant(boolean isTrident) {
    RegistryKeySet<ItemType> supported = mock();
    doReturn(isTrident).when(supported).contains(ItemTypeKeys.TRIDENT);
    doReturn(supported).when(enchant).getSupportedItems();

    EnchantData data = provider.of(enchant);

    assertThat(
        "Trident enchant status matches expected",
        data.isTridentEnchant(),
        is(isTrident)
    );
    verify(supported).contains(ItemTypeKeys.TRIDENT);
    verifyNoMoreInteractions(supported);
    verify(enchant).getSupportedItems();
    verifyNoMoreInteractions(enchant);
  }

}
