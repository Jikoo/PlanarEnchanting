package com.github.jikoo.planarenchanting.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import org.junit.jupiter.api.Test;

class BakedEnchantableDataTest {

  @Test
  void get() {
    assertThat("Values must be provided", BakedEnchantableData.get(), is(not(anEmptyMap())));
  }

}