package com.github.jikoo.planarenchanting.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.github.jikoo.planarenchanting.util.EnchantData;
import com.github.jikoo.planarenchanting.util.EnchantDataService;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;

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
@NullMarked
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EnchantingTableTest {

  private MockedStatic<Bukkit> bukkit;
  private final Random random = new Random(0);
  private Collection<Enchantment> toolEnchants;

  @BeforeAll
  void setUp() {
    bukkit = mockStatic(Bukkit.class);
    bukkit.when(() -> Bukkit.getRegistry(any())).thenAnswer(inv -> {
      Registry<Enchantment> registry = mock();
      doAnswer(invocation -> {
        NamespacedKey key = invocation.getArgument(0);
        Enchantment enchant = mock();
        doReturn(key).when(enchant).getKey();
        return enchant;
      }).when(registry).getOrThrow(any());
      return registry;
    });

    toolEnchants = List.of(
        Enchantment.EFFICIENCY,
        Enchantment.UNBREAKING,
        Enchantment.FORTUNE,
        Enchantment.SILK_TOUCH
    );
    setUpToolEnchants();
  }

  @AfterAll
  void tearDown() {
    bukkit.close();
  }

  @BeforeEach
  void setUpEach() {
    random.setSeed(0);
  }

  static void setUpToolEnchants() {
    EnchantData data = EnchantDataService.PROVIDER.of(Enchantment.EFFICIENCY);
    doReturn(10).when(data).getWeight();
    doAnswer(invocation -> {
      int level = invocation.getArgument(0);
      return 1 + level * 10;
    }).when(data).getMinModifiedCost(anyInt());
    doAnswer(invocation -> {
      int level = invocation.getArgument(0);
      return 51 + level * 10;
    }).when(data).getMaxModifiedCost(anyInt());
    doReturn(5).when(Enchantment.EFFICIENCY).getMaxLevel();

    data = EnchantDataService.PROVIDER.of(Enchantment.UNBREAKING);
    doReturn(5).when(data).getWeight();
    doAnswer(invocation -> {
      int level = invocation.getArgument(0);
      return 5 + level * 8;
    }).when(data).getMinModifiedCost(anyInt());
    doAnswer(invocation -> {
      int level = invocation.getArgument(0);
      return 55 + level * 8;
    }).when(data).getMaxModifiedCost(anyInt());
    doReturn(3).when(Enchantment.UNBREAKING).getMaxLevel();

    data = EnchantDataService.PROVIDER.of(Enchantment.FORTUNE);
    doReturn(2).when(data).getWeight();
    doAnswer(invocation -> {
      int level = invocation.getArgument(0);
      return 15 + level * 9;
    }).when(data).getMinModifiedCost(anyInt());
    doAnswer(invocation -> {
      int level = invocation.getArgument(0);
      return 65 + level * 9;
    }).when(data).getMaxModifiedCost(anyInt());
    doReturn(3).when(Enchantment.FORTUNE).getMaxLevel();

    data = EnchantDataService.PROVIDER.of(Enchantment.SILK_TOUCH);
    doReturn(1).when(data).getWeight();
    doReturn(15).when(data).getMinModifiedCost(anyInt());
    doReturn(65).when(data).getMaxModifiedCost(anyInt());
    doReturn(1).when(Enchantment.SILK_TOUCH).getMaxLevel();
  }

  @DisplayName("Empty enchantment list yields empty enchantments.")
  @Test
  void testEmptyEnchantList() {
    var operation = new EnchantingTable(List.of(), new Enchantability(10));

    assertThat(
        "Empty available enchants yields empty enchants",
        operation.apply(random, 1),
        is(anEmptyMap()));
  }

  @DisplayName("Enchantment incompatibility can be customized.")
  @Test
  void testAllIncompatibleAlwaysSingle() {
    var operation = new EnchantingTable(toolEnchants, new Enchantability(20));
    operation.setIncompatibility((a, b) -> true);

    assertThat(
        "All enchantments incompatible with others yields single enchantment",
        operation.apply(random, 1),
        is(aMapWithSize(1)));
  }

  @DisplayName("Enchantment level max can be modified.")
  @Test
  void testSetMaxLevel() {
    Enchantment enchant = Enchantment.EFFICIENCY;
    var operation = new EnchantingTable(List.of(enchant), new Enchantability(40));
    // Double max level for enchants that go over 1.
    operation.setMaxLevel(enchant1 -> enchant1.getMaxLevel() > 1 ? enchant1.getMaxLevel() * 2 : 1);

    String assertation = "High level enchantment generates higher level enchantments";
    random.setSeed(assertation.hashCode());
    assertThat(
        assertation,
        operation.apply(random, 50),
        both(hasEntry(is(enchant), greaterThan(enchant.getMaxLevel()))).and(aMapWithSize(1)));
  }

  @DisplayName("When enchantments are selected")
  @Nested
  class EnchantmentAttempt {

    private Map<Enchantment, Integer> selected;

    @BeforeEach
    void beforeEach() {
      var operation = new EnchantingTable(toolEnchants, new Enchantability(10));
      selected = operation.apply(random, random.nextInt(1, 31));
    }

    @DisplayName("One or more enchantments should be selected.")
    @Test
    void checkSize() {
      var operation = new EnchantingTable(toolEnchants, new Enchantability(10));
      selected = operation.apply(random, 30);
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

    private boolean conflicts(Enchantment enchantment1, Enchantment enchantment2) {
      if (enchantment1.equals(enchantment2)) {
        return true;
      }
      return enchantment1.conflictsWith(enchantment2) || enchantment2.conflictsWith(enchantment1);
    }

  }

  @DisplayName("Enchanting table button levels should be calculated consistently.")
  @ParameterizedTest
  @CsvSource({"1,0", "10,0", "15,0", "1,12348", "10,98124", "15,23479"})
  void testGetButtonLevels(int shelves, int seed) {
    random.setSeed(seed);
    int[] buttonLevels1 = EnchantingTable.getButtonLevels(random, shelves);
    random.setSeed(seed);
    int[] buttonLevels2 = EnchantingTable.getButtonLevels(random, shelves);

    assertThat("There are always three buttons", buttonLevels1.length, is(3));
    assertThat("There are always three buttons", buttonLevels2.length, is(3));

    List<Integer> buttonLevelsList1 = Arrays.stream(buttonLevels1).boxed().toList();

    assertThat(
        "Button levels should be generated consistently",
        buttonLevelsList1,
        contains(buttonLevels2[0], buttonLevels2[1], buttonLevels2[2]));
    assertThat(
        "Button levels must be positive integers that do not exceed 30",
        buttonLevelsList1,
        everyItem(is(both(lessThanOrEqualTo(30)).and(greaterThanOrEqualTo(0)))));
  }

}
