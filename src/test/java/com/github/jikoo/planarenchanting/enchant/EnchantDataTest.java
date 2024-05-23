package com.github.jikoo.planarenchanting.enchant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import com.github.jikoo.planarenchanting.util.mock.ServerMocks;
import com.github.jikoo.planarenchanting.util.mock.enchantments.EnchantmentMocks;
import com.github.jikoo.planarenchanting.util.mock.impl.InternalObject;
import java.util.stream.Stream;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Server;
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
    Server server = ServerMocks.mockServer();
    EnchantmentMocks.init(server);
  }

  @DisplayName("Enchantments must be explicitly supported.")
  @ParameterizedTest
  @MethodSource("getEnchants")
  void isPresent(Enchantment enchantment) {
    assertThat("Enchantment " + enchantment.getKey() + " must be supported", EnchantData.isPresent(enchantment));
  }

  @DisplayName("Unsupported enchantments must be supported via reflection.")
  @Test
  void testUnknownEnchant() {
    var enchant = (Enchantment & InternalObject<?>) mock(Enchantment.class, withSettings().extraInterfaces(InternalObject.class));
    NamespacedKey key = NamespacedKey.minecraft("fake_enchant");
    doReturn(key).when(enchant).getKey();
    doReturn(new net.minecraft.world.item.enchantment.Enchantment(key, value -> 5, value -> 10, 5, 2))
        .when(enchant).getHandle();
    EnchantmentMocks.putEnchant(enchant);

    var data = EnchantData.of(enchant);

    assertThat("Backing enchantment is identical", data.getEnchantment(), is(enchant));
    assertThat("Weight is expected value", data.getWeight(), is(5));
    assertThat("Anvil cost is expected value", data.getAnvilCost(), is(2));
    assertThat("Min quality is expected value", data.getMinCost(0), is(5));
    assertThat("Max quality is expected value", data.getMaxCost(0), is(10));
  }

  private static Stream<Enchantment> getEnchants() {
    return Registry.ENCHANTMENT.stream();
  }

}