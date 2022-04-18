package com.github.jikoo.planarenchanting.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.enchantments.EnchantmentMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import be.seeseemelk.mockbukkit.scheduler.BukkitSchedulerMock;
import com.github.jikoo.planarenchanting.util.EnchantmentHelper;
import com.github.jikoo.planarenchanting.util.mock.CraftEnchantMock;
import com.github.jikoo.planarenchanting.util.mock.MockHelper;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.InventoryView.Property;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Enchanting table utility methods")
@TestInstance(Lifecycle.PER_CLASS)
class EnchantingTableUtilTest {

  @BeforeAll
  void beforeAll() {
    MockBukkit.mock();
    EnchantmentHelper.setupToolEnchants();
  }

  @AfterAll
  void afterAll() {
    MockHelper.unmock();
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

  @DisplayName("Button updates send to user as expected.")
  @Test
  void testSendButtonUpdates() {
    var server = MockBukkit.getMock();

    var plugin = MockBukkit.createMockPlugin("SampleText");
    var offerData = new EnumMap<>(InventoryView.Property.class);

    var player = new PlayerMock(server, "sampletext") {
      @Override
      public boolean setWindowProperty(@NotNull InventoryView.Property prop, int value) {
        offerData.put(prop, value);
        return true;
      }
    };

    EnchantmentOffer[] offers = new EnchantmentOffer[] {
        new EnchantmentOffer(Enchantment.DIG_SPEED, 5, 1),
        new EnchantmentOffer(Enchantment.DURABILITY, 20, 2),
        new EnchantmentOffer(Enchantment.SILK_TOUCH, 30, 3)
    };

    assertDoesNotThrow(() -> EnchantingTable.updateButtons(plugin, player, offers));
    assertThat("Offer data is sent next tick", offerData, is(anEmptyMap()));

    BukkitSchedulerMock scheduler = server.getScheduler();

    assertDoesNotThrow(() -> scheduler.performTicks(1));
    assertThat("Offer data is sent", offerData, is(not(anEmptyMap())));

    // Ensure enchantment level requirements are sent correctly.
    assertThat(
        "First offer cost must be set",
        offerData.entrySet(),
        hasItem(Map.entry(Property.ENCHANT_BUTTON1, offers[0].getCost())));
    assertThat(
        "Second offer cost must be set",
        offerData.entrySet(),
        hasItem(Map.entry(Property.ENCHANT_BUTTON2, offers[1].getCost())));
    assertThat(
        "Third offer cost must be set",
        offerData.entrySet(),
        hasItem(Map.entry(Property.ENCHANT_BUTTON3, offers[2].getCost())));

    // Ensure enchantment levels are sent correctly.
    assertThat(
        "First offer level must be set",
        offerData.entrySet(),
        hasItem(Map.entry(Property.ENCHANT_LEVEL1, offers[0].getEnchantmentLevel())));
    assertThat(
        "Second offer level must be set",
        offerData.entrySet(),
        hasItem(Map.entry(Property.ENCHANT_LEVEL2, offers[1].getEnchantmentLevel())));
    assertThat(
        "Third offer level must be set",
        offerData.entrySet(),
        hasItem(Map.entry(Property.ENCHANT_LEVEL3, offers[2].getEnchantmentLevel())));

    // Exact IDs not verified - can't really test this without NMS.
    assertThat(
        "Offer contains enchantment IDs",
        offerData.keySet(),
        hasItems(Property.ENCHANT_ID1, Property.ENCHANT_ID2, Property.ENCHANT_ID3));
  }

  @DisplayName("Fetching Enchantment ID should not cause errors.")
  @Test
  void testInvalidGetEnchantmentId() {
    var server = MockBukkit.getMock();

    var plugin = MockBukkit.createMockPlugin("SampleText");

    var player = new PlayerMock(server, "sampletext") {
      @Override
      public boolean setWindowProperty(@NotNull InventoryView.Property prop, int value) {
        return true;
      }
    };

    String enchantName = "enchant_table_util";
    var unregisteredEnchantment = new EnchantmentMock(NamespacedKey.minecraft(enchantName + 1), enchantName + 1);
    var registeredReflectableEnchant = new CraftEnchantMock(NamespacedKey.minecraft(enchantName + 2), 1, value -> value, value -> value);
    EnchantmentHelper.putEnchant(registeredReflectableEnchant);
    var registeredUnlistedNonReflectableEnchant = new EnchantmentMock(NamespacedKey.minecraft(enchantName + 3), enchantName + 3);

    EnchantmentOffer[] offers = new EnchantmentOffer[] {
      new EnchantmentOffer(unregisteredEnchantment, 5, 1),
      new EnchantmentOffer(registeredReflectableEnchant, 20, 2),
      new EnchantmentOffer(registeredUnlistedNonReflectableEnchant, 30, 3)
    };

    // Schedule button update.
    EnchantingTable.updateButtons(plugin, player, offers);

    BukkitSchedulerMock scheduler = server.getScheduler();
    assertDoesNotThrow(() -> scheduler.performTicks(1));
  }

}