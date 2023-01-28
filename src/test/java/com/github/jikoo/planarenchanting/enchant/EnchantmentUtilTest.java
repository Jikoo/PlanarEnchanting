package com.github.jikoo.planarenchanting.enchant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;

import com.github.jikoo.planarenchanting.util.mock.inventory.ItemFactoryMocks;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@DisplayName("Enchantment utility methods")
@TestInstance(Lifecycle.PER_CLASS)
class EnchantmentUtilTest {

  @Test
  void testEnchantsEmptyIfNull() {
    assertThat("Null meta is empty", EnchantmentUtil.getEnchants(null), is(anEmptyMap()));
  }

  @Test
  void testGetEnchantsStorageMeta() {
    var meta = ItemFactoryMocks.createMeta(EnchantmentStorageMeta.class);

    assertThat("Meta is empty", EnchantmentUtil.getEnchants(meta), is(anEmptyMap()));

    Map<Enchantment, Integer> enchantments = new HashMap<>();
    enchantments.put(Enchantment.DIG_SPEED, 10);
    enchantments.put(Enchantment.LUCK, 5);

    for (Entry<Enchantment, Integer> enchant : enchantments.entrySet()) {
      meta.addStoredEnchant(enchant.getKey(), enchant.getValue(), true);
    }

    assertThat("Enchantments must be retrieved from result",
        EnchantmentUtil.getEnchants(meta).entrySet(),
        both(everyItem(is(in(enchantments.entrySet())))).and(
            containsInAnyOrder(enchantments.entrySet().toArray())));
  }

  @Test
  void testSetEnchantsStorageMeta() {
    var meta = ItemFactoryMocks.createMeta(EnchantmentStorageMeta.class);

    assertThat("Meta is empty", EnchantmentUtil.getEnchants(meta), is(anEmptyMap()));

    Map<Enchantment, Integer> enchantments = new HashMap<>();
    enchantments.put(Enchantment.DIG_SPEED, 10);
    enchantments.put(Enchantment.LUCK, 5);

    EnchantmentUtil.addEnchants(meta, enchantments);

    assertThat("Enchantments must be retrieved from result",
        meta.getStoredEnchants().entrySet(),
        both(everyItem(is(in(enchantments.entrySet())))).and(
            containsInAnyOrder(enchantments.entrySet().toArray())));
  }

  @Test
  void testGetEnchants() {
    var meta = ItemFactoryMocks.createMeta(ItemMeta.class);

    assertThat("Meta is empty", EnchantmentUtil.getEnchants(meta), is(anEmptyMap()));

    Map<Enchantment, Integer> enchantments = new HashMap<>();
    enchantments.put(Enchantment.DIG_SPEED, 10);
    enchantments.put(Enchantment.LUCK, 5);

    for (Entry<Enchantment, Integer> enchant : enchantments.entrySet()) {
      meta.addEnchant(enchant.getKey(), enchant.getValue(), true);
    }

    assertThat("Enchantments must be retrieved from result",
        EnchantmentUtil.getEnchants(meta).entrySet(),
        both(everyItem(is(in(enchantments.entrySet())))).and(
            containsInAnyOrder(enchantments.entrySet().toArray())));
  }

  @Test
  void testSetEnchants() {
    var meta = ItemFactoryMocks.createMeta(ItemMeta.class);

    assertThat("Meta is empty", EnchantmentUtil.getEnchants(meta), is(anEmptyMap()));

    Map<Enchantment, Integer> enchantments = new HashMap<>();
    enchantments.put(Enchantment.DIG_SPEED, 10);
    enchantments.put(Enchantment.LUCK, 5);

    EnchantmentUtil.addEnchants(meta, enchantments);

    assertThat("Enchantments must be retrieved from result",
        meta.getEnchants().entrySet(),
        both(everyItem(is(in(enchantments.entrySet())))).and(
            containsInAnyOrder(enchantments.entrySet().toArray())));
  }

}