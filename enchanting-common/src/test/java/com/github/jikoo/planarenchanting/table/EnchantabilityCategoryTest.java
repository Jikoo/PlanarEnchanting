package com.github.jikoo.planarenchanting.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;

class EnchantabilityCategoryTest {

  private static final String REAL_CATEGORY = "STONE_TOOL";
  private static final String NOT_A_CATEGORY = "Java Developer Eats Bugs LIVE On Stream For Your Viewing Pleasure!";

  @Test
  void get() {
    assertThat(
        "Enchantment categories are available",
        EnchantabilityCategory.get(REAL_CATEGORY),
        is(notNullValue())
    );
  }

  @Test
  void getNotPresent() {
    assertThat(
        "Nonexistent categories fail gracefully",
        EnchantabilityCategory.get(NOT_A_CATEGORY),
        is(nullValue())
    );
  }

}
