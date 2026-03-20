package com.github.jikoo.planarenchanting.util;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@DisplayName("Item utility methods")
@TestInstance(Lifecycle.PER_CLASS)
class ItemUtilTest {

  @Test
  void testAirConstantImmutable() {
    assertThrows(UnsupportedOperationException.class, () -> ItemUtil.AIR.setType(Material.DIRT));
  }

}
