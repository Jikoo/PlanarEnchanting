package com.github.jikoo.planarenchanting.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.github.jikoo.planarenchanting.util.mock.ServerMocks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EnchantabilitiesTest {

  private static final String REAL_CATEGORY = "STONE_TOOL";
  private static final String NOT_A_CATEGORY = "Java Developer Eats Bugs LIVE On Stream For Your Viewing Pleasure!";

  @BeforeAll
  static void setUp() {
    // Enchantability checks server defaults or listings, server should be set up to be safe.
    ServerMocks.mockServer();
  }

  @Test
  void get() {
    assertThat(
        "Enchantment categories are available",
        Enchantabilities.get(REAL_CATEGORY),
        is(notNullValue())
    );
  }

  @Test
  void getNotPresent() {
    assertThat(
        "Nonexistent categories fail gracefully",
        Enchantabilities.get(NOT_A_CATEGORY),
        is(nullValue())
    );
  }

  @Test
  void getOrDefault() {
    assertThat(
        "Available categories are fetched",
        Enchantabilities.getOrDefault(REAL_CATEGORY, () -> new Enchantability(1)),
        both(is(notNullValue())).and(is(Enchantabilities.get(REAL_CATEGORY)))
    );
  }

  @Test
  void getOrDefaultDefault() {
    Enchantability def = new Enchantability(100);
    assertThat(
        "Nonexistent categories fall through to default",
        Enchantabilities.getOrDefault(NOT_A_CATEGORY, () -> def),
        is(def)
    );
  }

}