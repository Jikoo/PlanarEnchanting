package com.github.jikoo.planarenchanting.enchant;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import com.github.jikoo.planarenchanting.util.mock.ServerMocks;
import com.github.jikoo.planarenchanting.util.mock.enchantments.EnchantmentMocks;
import com.github.jikoo.planarenchanting.util.mock.impl.InternalObject;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.world.item.enchantment.Enchantment.Rarity;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Server;
import org.bukkit.enchantments.Enchantment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import sun.misc.Unsafe;

/**
 * Unit tests for unsupported new enchantments.
 *
 * <p>As a developer, I want to be able to support new enchantments
 * without additional maintanence.
 *
 * <p><b>Feature:</b> Sanely support unexpected enchantments
 * <br><b>Given</b> I am a developer
 * <br><b>When</b> I attempt to obtain information about a new enchantment
 * <br><b>Then</b> the information should be available or a sane default will be provided
 */
@DisplayName("Feature: Attempt support for unknown enchantments.")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EnchantmentReflectionTest {

  private Enchantment brokenRegisteredEnchant;

  @BeforeAll
  void beforeAll() {
    Server server = ServerMocks.mockServer();
    Bukkit.setServer(server);
    EnchantmentMocks.init(server);

    Registry.ENCHANTMENT.stream().map(enchantment -> {
      // Keep mending default to check fallthrough.
      if (enchantment.equals(Enchantment.MENDING)) {
        return enchantment;
      }

      var internalEnchant = (Enchantment & InternalObject<?>) mock(Enchantment.class, withSettings().extraInterfaces(InternalObject.class));
      doReturn(enchantment.getKey()).when(internalEnchant).getKey();
      doReturn(enchantment.getStartLevel()).when(internalEnchant).getStartLevel();
      doReturn(enchantment.getMaxLevel()).when(internalEnchant).getMaxLevel();
      // Don't need to copy rest of data over
      EnchantData data = EnchantData.of(enchantment);
      doReturn(new net.minecraft.world.item.enchantment.Enchantment(
          enchantment.getKey(), data::getMinCost, data::getMaxCost, new Rarity(data.getWeight())))
          .when(internalEnchant).getHandle();

      return internalEnchant;
    }).forEach(EnchantmentMocks::putEnchant);

    var brokenInternal = (Enchantment & InternalObject<?>) mock(Enchantment.class, withSettings().extraInterfaces(InternalObject.class));
    NamespacedKey key = NamespacedKey.minecraft("fake_enchant1");
    doReturn(key).when(brokenInternal).getKey();
    doReturn(
        new net.minecraft.world.item.enchantment.Enchantment(
            key,
            value -> {
              Unsafe.getUnsafe().throwException(new IllegalAccessException("hello world"));
              return 5;
            },
            value -> {
              Unsafe.getUnsafe().throwException(new InvocationTargetException(new Exception("hello world")));
              return 5;
            },
            new Rarity(-5)
        )
    ).when(brokenInternal).getHandle();
    EnchantmentMocks.putEnchant(brokenInternal);
    brokenRegisteredEnchant = brokenInternal;
  }

  private Stream<Named<Enchantment>> streamEnchants() {
    return Registry.ENCHANTMENT.stream()
        // Mending is not set up so that it can be used to test fallthrough.
        .filter(enchantment ->
            !enchantment.equals(Enchantment.MENDING)
                && !enchantment.equals(brokenRegisteredEnchant))
        .map(enchantment -> Named.of(enchantment.getKey().toString(), enchantment));
  }

  private Stream<Arguments> streamEnchantLevels() {
    return streamEnchants().flatMap(
        enchantment ->
            enchantment.getPayload().getMaxLevel() < 1
                ? Stream.of(Arguments.of(enchantment, 1))
                : IntStream.range(1, enchantment.getPayload().getMaxLevel() + 1).mapToObj(value -> Arguments.of(enchantment, value)));
  }

  @DisplayName("Reflection should grab minimum quality method or fall through gracefully.")
  @ParameterizedTest
  @MethodSource("streamEnchantLevels")
  void testReflectiveMin(Enchantment enchantment, int level) {
    EnchantData enchantData = EnchantData.of(enchantment);
    assertThat("Reflection should provide expected value",
        enchantData.getMinCost(level),
        is(EnchantDataReflection.getMinCost(enchantment).applyAsInt(level)));
  }

  @DisplayName("Reflection should grab maximum quality method or fall through gracefully.")
  @ParameterizedTest
  @MethodSource("streamEnchantLevels")
  void testReflectiveMax(Enchantment enchantment, int level) {
    EnchantData enchantData = EnchantData.of(enchantment);
    assertThat("Reflection should provide expected value",
        enchantData.getMaxCost(level),
        is(EnchantDataReflection.getMaxCost(enchantment).applyAsInt(level)));
  }

  @DisplayName("Reflection should grab minimum method or fall through gracefully.")
  @ParameterizedTest
  @MethodSource("streamEnchants")
  void testReflectiveRarity(Enchantment enchantment) {
    EnchantData enchantData = EnchantData.of(enchantment);
    assertThat("Reflection should provide expected value",
        enchantData.getRarity(), is(EnchantDataReflection.getRarity(enchantment)));
  }

  @DisplayName("Reflection should provide usable defaults.")
  @ParameterizedTest
  @MethodSource("getBrokenEnchants")
  void testReflectiveDefaults(Enchantment broken) {
    assertThat("Reflection should fall through gracefully", 1,
        is(EnchantDataReflection.getMinCost(broken).applyAsInt(0)));
    assertThat("Reflection should fall through gracefully", 21,
        is(EnchantDataReflection.getMinCost(broken).applyAsInt(2)));
    assertThat("Reflection should fall through gracefully", 6,
        is(EnchantDataReflection.getMaxCost(broken).applyAsInt(0)));
    assertThat("Reflection should fall through gracefully", 26,
        is(EnchantDataReflection.getMaxCost(broken).applyAsInt(2)));
    assertThat("Reflection should fall through gracefully", 0,
        is(EnchantDataReflection.getRarity(broken).getWeight()));
  }

  public Stream<Named<Enchantment>> getBrokenEnchants() {
    var validUnregisteredEnchant = (Enchantment & InternalObject<?>) mock(Enchantment.class, withSettings().extraInterfaces(InternalObject.class));
    NamespacedKey key = NamespacedKey.minecraft("fake_enchant2");
    doReturn(key).when(validUnregisteredEnchant).getKey();
    doReturn(new net.minecraft.world.item.enchantment.Enchantment(key, value -> 5, value -> 10, new Rarity(5)))
        .when(validUnregisteredEnchant).getHandle();

    return Stream.of(
        // Present but not fake NMS.
        Named.of(Enchantment.MENDING.getKey().toString(), Enchantment.MENDING),
        // Not present.
        Named.of(validUnregisteredEnchant.getKey().toString(), validUnregisteredEnchant),
        // Present but throws exceptions on access.
        Named.of(brokenRegisteredEnchant.getKey().toString(), brokenRegisteredEnchant)
    );
  }
}
