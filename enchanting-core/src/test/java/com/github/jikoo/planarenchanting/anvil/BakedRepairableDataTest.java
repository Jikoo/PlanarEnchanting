package com.github.jikoo.planarenchanting.anvil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import org.junit.jupiter.api.Test;

class BakedRepairableDataTest {

  @Test
  void getTags() {
    assertThat("Values must be provided", BakedRepairableData.getTags(), is(not(anEmptyMap())));
  }

  @Test
  void getLists() {
    // Hopefully Mojang adds tags for other repairables and this whole method can be removed.
    assertThat("Values must be provided", BakedRepairableData.getLists(), is(not(anEmptyMap())));
  }

}