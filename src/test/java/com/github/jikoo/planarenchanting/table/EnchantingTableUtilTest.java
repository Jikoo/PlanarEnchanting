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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.jikoo.planarenchanting.util.mock.ServerMocks;
import com.github.jikoo.planarenchanting.util.mock.enchantments.EnchantmentMocks;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Server;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

@DisplayName("Enchanting table utility methods")
@TestInstance(Lifecycle.PER_CLASS)
class EnchantingTableUtilTest {

  @BeforeAll
  void beforeAll() {
    Server server = ServerMocks.mockServer();
    Bukkit.setServer(server);
    EnchantmentMocks.init(server);
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
    var scheduler = mock(BukkitScheduler.class);
    ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);
    ArgumentCaptor<Long> delayCaptor = ArgumentCaptor.forClass(Long.class);
    when(scheduler.runTaskLater(any(Plugin.class), taskCaptor.capture(), delayCaptor.capture())).thenReturn(null);

    var server = Bukkit.getServer();
    when(server.getScheduler()).thenReturn(scheduler);

    var plugin = mock(Plugin.class);
    var player = mock(Player.class);
    var offerData = new EnumMap<>(InventoryView.Property.class);
    when(player.setWindowProperty(any(Property.class), anyInt())).thenAnswer(invocation -> {
      offerData.put(invocation.getArgument(0), invocation.getArgument(1));
      return true;
    });

    EnchantmentOffer[] offers = new EnchantmentOffer[] {
        new EnchantmentOffer(Enchantment.DIG_SPEED, 5, 1),
        new EnchantmentOffer(Enchantment.DURABILITY, 20, 2),
        new EnchantmentOffer(Enchantment.SILK_TOUCH, 30, 3)
    };

    assertDoesNotThrow(() -> EnchantingTable.updateButtons(plugin, player, offers));
    assertThat("Offer data is not sent immediately", offerData, is(anEmptyMap()));
    assertThat("Delay is 1 tick", delayCaptor.getValue(), is(1L));

    Runnable task = taskCaptor.getValue();

    assertDoesNotThrow(task::run);
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

    assertThat(
        "Offer contains enchantment IDs",
        offerData.keySet(),
        hasItems(Property.ENCHANT_ID1, Property.ENCHANT_ID2, Property.ENCHANT_ID3));
    // Exact IDs not verified - can't really test this without NMS.
    // If this does break in the future, either it is really broken or protection is no longer ID 0.
    assertThat(
        "Offer values are not default",
        offerData.values(),
        not(hasItem(0)));
  }

  @DisplayName("Fetching Enchantment ID should not cause errors.")
  @Test
  void testInvalidGetEnchantmentId() {
    var scheduler = mock(BukkitScheduler.class);
    ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);
    when(scheduler.runTaskLater(any(Plugin.class), taskCaptor.capture(), anyLong())).thenReturn(null);

    var server = Bukkit.getServer();
    when(server.getScheduler()).thenReturn(scheduler);

    var plugin = mock(Plugin.class);
    var player = mock(Player.class);

    String enchantName = "enchant_table_util";
    var unregistered = enchantment(enchantName + "1");
    var registered = enchantment(enchantName + "2");
    EnchantmentMocks.putEnchant(registered);
    var invalidState = enchantment(enchantName + "3");
    EnchantmentMocks.putEnchant(invalidState);

    // Return empty iterator for second invocation to hit illegal state.
    doAnswer(invocation -> Registry.ENCHANTMENT.stream().iterator())
        .doAnswer(invocation -> Stream.empty().iterator())
        .when(Registry.ENCHANTMENT).iterator();

    EnchantmentOffer[] offers = new EnchantmentOffer[] {
      new EnchantmentOffer(unregistered, 5, 1),
      new EnchantmentOffer(registered, 20, 2),
      new EnchantmentOffer(invalidState, 30, 3)
    };

    // Schedule button update.
    EnchantingTable.updateButtons(plugin, player, offers);

    var task = taskCaptor.getValue();
    assertDoesNotThrow(task::run);

    // Un-break iterator post-test just in case.
    doAnswer(invocation -> Registry.ENCHANTMENT.stream().iterator())
        .when(Registry.ENCHANTMENT).iterator();
  }

  private Enchantment enchantment(String key) {
    Enchantment enchantment = mock();
    doReturn(NamespacedKey.minecraft(key)).when(enchantment).getKey();
    doReturn(1).when(enchantment).getStartLevel();
    doReturn(1).when(enchantment).getMaxLevel();
    doReturn(EnchantmentTarget.VANISHABLE).when(enchantment).getItemTarget();
    return enchantment;
  }

}