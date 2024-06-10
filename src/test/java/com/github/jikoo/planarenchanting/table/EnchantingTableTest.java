package com.github.jikoo.planarenchanting.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

import com.github.jikoo.planarenchanting.util.mock.ServerMocks;
import com.github.jikoo.planarenchanting.util.mock.enchantments.EnchantmentMocks;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * Unit tests for enchantments.
 *
 * <p>As a developer, I want to be able to generate enchantments
 * because I would like to support enchanting tables.
 *
 * <p><b>Feature:</b> Calculate enchantments for special items
 * <br><b>Given</b> I am a user
 * <br><b>When</b> I attempt to enchant an item
 * <br><b>And</b> the item is a special item
 * <br><b>Then</b> the item should recieve applicable enchantments
 */
@DisplayName("Feature: Calculate enchantments for enchanting tables.")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EnchantingTableTest {

  private static final Random RANDOM = new Random(0);
  private static Collection<Enchantment> toolEnchants;

  @BeforeAll
  void beforeAll() {
    ServerMocks.mockServer();
    EnchantmentMocks.init();
    toolEnchants = List.of(
        Enchantment.EFFICIENCY,
        Enchantment.UNBREAKING,
        Enchantment.FORTUNE,
        Enchantment.SILK_TOUCH);
  }

  @DisplayName("Empty enchantment list yields empty enchantments.")
  @Test
  void testEmptyEnchantList() {
    var operation = new EnchantingTable(List.of(), Enchantability.STONE);

    assertThat(
        "Empty available enchants yields empty enchants",
        operation.apply(RANDOM, 1),
        is(anEmptyMap()));
  }

  @DisplayName("Enchantment incompatibility can be customized.")
  @Test
  void testAllIncompatibleAlwaysSingle() {
    var operation = new EnchantingTable(toolEnchants, Enchantability.GOLD_ARMOR);
    operation.setIncompatibility((a, b) -> true);

    assertThat(
        "All enchantments incompatible with others yields single enchantment",
        operation.apply(RANDOM, 1),
        is(aMapWithSize(1)));
  }

  @DisplayName("Enchantment level max can be modified.")
  @Test
  void testSetMaxLevel() {
    Enchantment enchant = Enchantment.EFFICIENCY;
    var operation = new EnchantingTable(List.of(enchant), Enchantability.GOLD_ARMOR);
    // Double max level for enchants that go over 1.
    operation.setMaxLevel(enchant1 -> enchant1.getMaxLevel() > 1 ? enchant1.getMaxLevel() * 2 : 1);

    String assertation = "High level enchantment generates higher level enchantments";
    RANDOM.setSeed(assertation.hashCode());
    assertThat(
        assertation,
        operation.apply(RANDOM, 50),
        both(hasEntry(is(enchant), greaterThan(enchant.getMaxLevel()))).and(aMapWithSize(1)));
  }

  @DisplayName("When enchantments are selected")
  @Nested
  class EnchantmentAttempt {

    private Map<Enchantment, Integer> selected;

    @BeforeEach
    void beforeEach() {
      var operation = new EnchantingTable(toolEnchants, Enchantability.STONE);
      selected = operation.apply(RANDOM, RANDOM.nextInt(1, 31));
    }

    @DisplayName("One or more enchantments should be selected.")
    @Test
    void checkSize() {
      var operation = new EnchantingTable(toolEnchants, Enchantability.STONE);
      selected = operation.apply(RANDOM, 30);
      assertThat(
          "One or more enchantments must be selected",
          selected,
          is(aMapWithSize(greaterThan(0))));
    }

    @DisplayName("Enchantments should not conflict.")
    @RepeatedTest(10)
    void checkConflict() {
      Enchantment[] enchantments = selected.keySet().toArray(new Enchantment[0]);
      for (int i = 0; i < enchantments.length; ++i) {
        for (int j = 0; j < enchantments.length; ++j) {
          if (i == j) {
            continue;
          }
          assertThat(
              "Enchantments may not conflict",
              conflicts(enchantments[i], enchantments[j]),
              is(false));
        }
      }
    }

    private boolean conflicts(
        @NotNull Enchantment enchantment1,
        @NotNull Enchantment enchantment2) {
      if (enchantment1.equals(enchantment2)) {
        return true;
      }
      return enchantment1.conflictsWith(enchantment2) || enchantment2.conflictsWith(enchantment1);
    }

  }

}
