package com.github.jikoo.planarenchanting.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.jikoo.planarenchanting.util.mock.inventory.ItemFactoryMocks;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Repairable;
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

  @Test
  void testEmptyIfNull() {
    assertThat("Null ItemStack is empty", ItemUtil.isEmpty(null));
  }

  @Test
  void testEmptyIfAir() {
    assertThat("Air ItemStack is empty", ItemUtil.isEmpty(new ItemStack(Material.AIR)));
  }

  @Test
  void testEmptyIfNegative() {
    var item = new ItemStack(Material.DIRT);
    item.setAmount(-5);
    assertThat("Negative ItemStack is empty", ItemUtil.isEmpty(item));
  }

  @Test
  void testNotEmpty() {
    assertThat(
        "Normal ItemStack is not empty",
        ItemUtil.isEmpty(new ItemStack(Material.DIRT)),
        is(false));
  }

  @Test
  void testRepairCostNonRepairable() {
    assertThat("Non-repairable meta is 0 cost", ItemUtil.getRepairCost(null), is(0));
  }

  @Test
  void testRepairCostRepairable() {
    var value = 10;
    var meta = ItemFactoryMocks.createMeta(Repairable.class);
    meta.setRepairCost(value);
    assertThat("Repairable returns cost", ItemUtil.getRepairCost(meta), is(value));
  }

}