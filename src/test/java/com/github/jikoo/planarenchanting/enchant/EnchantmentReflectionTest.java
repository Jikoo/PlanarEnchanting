package com.github.jikoo.planarenchanting.enchant;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import be.seeseemelk.mockbukkit.MockBukkit;
import com.github.jikoo.planarenchanting.util.EnchantmentHelper;
import com.github.jikoo.planarenchanting.util.mock.CraftEnchantMock;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
import com.github.jikoo.planarenchanting.util.mock.MockHelper;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
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
    MockBukkit.mock();
    EnchantmentHelper.setupToolEnchants();
    EnchantmentHelper.getRegisteredEnchantments().stream().map(enchantment -> {
      // Keep mending default to check fallthrough.
      if (enchantment.equals(Enchantment.MENDING)) {
        return enchantment;
      }
      EnchantData data = EnchantData.of(enchantment);
      return new CraftEnchantMock(enchantment, data.getWeight(),
          data::getMinCost, data::getMaxCost);
    }).forEach(EnchantmentHelper::putEnchant);
     brokenRegisteredEnchant = new CraftEnchantMock(
        NamespacedKey.minecraft("fake_enchant1"),
        -5,
         value -> {
           Unsafe.getUnsafe().throwException(new IllegalAccessException("hello world"));
           return 5;
         },
        value -> {
          Unsafe.getUnsafe().throwException(new InvocationTargetException(new Exception("hello world")));
          return 5;
        });
    EnchantmentHelper.putEnchant(brokenRegisteredEnchant);
  }

  @AfterAll
  void afterAll() {
    MockHelper.unmock();
  }

  private Stream<Enchantment> enchantmentStream() {
    return EnchantmentHelper.getRegisteredEnchantments().stream()
        // Mending is not set up so that it can be used to test fallthrough.
        .filter(enchantment ->
            !enchantment.equals(Enchantment.MENDING)
                && !enchantment.equals(brokenRegisteredEnchant));
  }

  private int getRandomLevel(Enchantment enchantment) {
    return enchantment.getMaxLevel() > 0 ?
        ThreadLocalRandom.current().nextInt(enchantment.getMaxLevel()) + 1 : 1;
  }

  @DisplayName("Reflection should grab minimum quality method or fall through gracefully.")
  @ParameterizedTest
  @MethodSource("enchantmentStream")
  void testReflectiveMin(Enchantment enchantment) {
    EnchantData enchantData = EnchantData.of(enchantment);
    int level = getRandomLevel(enchantment);
    assertThat("Reflection should provide expected value",
        enchantData.getMinCost(level),
        is(EnchantDataReflection.getMinCost(enchantment).applyAsInt(level)));
  }

  @DisplayName("Reflection should grab maximum quality method or fall through gracefully.")
  @ParameterizedTest
  @MethodSource("enchantmentStream")
  void testReflectiveMax(Enchantment enchantment) {
    EnchantData enchantData = EnchantData.of(enchantment);
    int level = getRandomLevel(enchantment);
    assertThat("Reflection should provide expected value",
        enchantData.getMaxCost(level),
        is(EnchantDataReflection.getMaxCost(enchantment).applyAsInt(level)));
  }

  @DisplayName("Reflection should grab minimum method or fall through gracefully.")
  @ParameterizedTest
  @MethodSource("enchantmentStream")
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

  public Stream<Enchantment> getBrokenEnchants() {
    return Stream.of(
        // Present but not fake NMS.
        Enchantment.MENDING,
        // Not present.
        new CraftEnchantMock(
            NamespacedKey.minecraft("fake_enchant2"),
            5,
            value -> 5,
            value -> 10),
        // Present but throws exceptions on access.
        brokenRegisteredEnchant
    );
  }
}
