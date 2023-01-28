package com.github.jikoo.planarenchanting.enchant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.jikoo.planarenchanting.util.mock.enchantments.InternalEnchantmentHolder;
import com.github.jikoo.planarenchanting.util.mock.enchantments.EnchantmentMocks;
import java.util.Collection;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Expose extra enchantment data")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EnchantDataTest {

  @BeforeAll
  void beforeAll() {
    EnchantmentMocks.init();
  }

  @DisplayName("Enchantments must be explicitly supported.")
  @ParameterizedTest
  @MethodSource("getEnchants")
  void isPresent(Enchantment enchantment) {
    assertThat("Enchantment must be supported", EnchantData.isPresent(enchantment));
  }

  @DisplayName("Unsupported enchantments must be supported via reflection.")
  @Test
  void testUnknownEnchant() {
    var enchant = new InternalEnchantmentHolder(
        NamespacedKey.minecraft("fake_enchant"),
        5,
        value -> 5,
        value -> 10);
    EnchantmentMocks.putEnchant(enchant);

    var data = EnchantData.of(enchant);

    assertThat("Backing enchantment is identical", data.getEnchantment(), is(enchant));
    assertThat("Weight is expected value", data.getWeight(), is(5));
    assertThat("Rarity is expected value", data.getRarity(), is(EnchantRarity.UNCOMMON));
    assertThat("Min quality is expected value", data.getMinCost(0), is(5));
    assertThat("Max quality is expected value", data.getMaxCost(0), is(10));
  }

  static Collection<Enchantment> getEnchants() {
    return EnchantmentMocks.getRegisteredEnchantments();
  }

}