package com.github.jikoo.planarenchanting.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import com.github.jikoo.planarenchanting.util.mock.ServerMocks;
import com.github.jikoo.planarenchanting.util.mock.enchantments.EnchantmentMocks;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Enchanting table utility methods")
@TestInstance(Lifecycle.PER_CLASS)
class EnchantingTableUtilTest {

  @BeforeAll
  void beforeAll() {
    ServerMocks.mockServer();
    EnchantmentMocks.init();
  }

  @DisplayName("Enchanting table button levels should be calculated consistently.")
  @ParameterizedTest
  @CsvSource({"1,0", "10,0", "15,0", "1,12348", "10,98124", "15,23479"})
  void testGetButtonLevels(int shelves, int seed) {
    Random random = new Random(seed);
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