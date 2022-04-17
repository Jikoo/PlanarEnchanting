package com.github.jikoo.planarenchanting.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.github.jikoo.planarenchanting.util.EnchantmentHelper;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@DisplayName("Feature: Enchant blocks in enchanting tables.")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TableEnchantListenerTest {

  private static final Collection<Enchantment> TOOL_ENCHANTS = List.of(
      Enchantment.DIG_SPEED,
      Enchantment.DURABILITY,
      Enchantment.LOOT_BONUS_BLOCKS,
      Enchantment.SILK_TOUCH);
  private static final Material ENCHANTABLE_MATERIAL = Material.COAL_ORE;
  private static final Material UNENCHANTABLE_MATERIAL = Material.DIRT;
  private static final Enchantment VALID_ENCHANT = Enchantment.DIG_SPEED;

  private ServerMock server;
  private Plugin plugin;
  private TableEnchantListener listener;
  private Player player;
  private ItemStack itemStack;
  private NamespacedKey key;

  @BeforeAll
  void setUpAll() {
    server = MockBukkit.mock();
    server.addSimpleWorld("world");
    EnchantmentHelper.setupToolEnchants();
  }

  @BeforeEach
  void setUp() throws NoSuchMethodException {
    if (plugin != null) {
      HandlerList.unregisterAll(plugin);
    }

    plugin = MockBukkit.createMockPlugin("SampleText");

    listener = new TableEnchantListener(plugin) {
      private final EnchantingTable table = new EnchantingTable(TOOL_ENCHANTS, Enchantability.STONE);

      @Override
      protected boolean isIneligible(@NotNull Player player,
          @NotNull ItemStack enchanted) {
        return itemStack.getType() != ENCHANTABLE_MATERIAL;
      }

      @Override
      protected @NotNull EnchantingTable getTable(@NotNull Player player,
          @NotNull ItemStack enchanted) {
        return table;
      }
    };

    // Manually register events - MockBukkit doesn't seem capable of finding private methods.
    // CB is very capable of this, locates event handlers on the classloader level.
    var onPrepareItemEnchant = TableEnchantListener.class
        .getDeclaredMethod("onPrepareItemEnchant", PrepareItemEnchantEvent.class);
    onPrepareItemEnchant.setAccessible(true);
    PrepareItemEnchantEvent.getHandlerList().register(new RegisteredListener(listener,
        (listener, event) -> {
          try {
            onPrepareItemEnchant.invoke(listener, event);
          } catch (IllegalAccessException | InvocationTargetException e) {
            throw new EventException(e);
          }
        },
        EventPriority.NORMAL,
        plugin,
        false));
    var onEnchantItem = TableEnchantListener.class
        .getDeclaredMethod("onEnchantItem", EnchantItemEvent.class);
    onEnchantItem.setAccessible(true);
    EnchantItemEvent.getHandlerList().register(new RegisteredListener(listener,
        (listener, event) -> {
          try {
            onEnchantItem.invoke(listener, event);
          } catch (IllegalAccessException | InvocationTargetException e) {
            throw new EventException(e);
          }
        },
        EventPriority.NORMAL,
        plugin,
        false));
    System.out.printf("Prepare: %s%n", PrepareItemEnchantEvent.getHandlerList().getRegisteredListeners().length);
    System.out.printf("Do: %s%n", EnchantItemEvent.getHandlerList().getRegisteredListeners().length);

    player = new PlayerMock(server, "sampletext");
    itemStack = new ItemStack(ENCHANTABLE_MATERIAL);
    key = new NamespacedKey(plugin, "enchanting_table_seed");
  }

  @AfterAll
  void tearDownAll() {
    MockBukkit.unmock();
  }

  @Test
  void testCanNotEnchantEnchanted() {
    itemStack.addUnsafeEnchantment(VALID_ENCHANT, 10);
    assertThat("Enchanted item cannot be enchanted", listener.canNotEnchant(player, itemStack));
  }

  @Test
  void testCanNotEnchantStack() {
    itemStack.setAmount(2);
    assertThat("Stacked item cannot be enchanted", listener.canNotEnchant(player, itemStack));
  }

  @Test
  void testCanNotEnchantWrongMaterial() {
    itemStack.setType(UNENCHANTABLE_MATERIAL);
    assertThat(
        "Material with no enchants cannot be enchanted",
        listener.canNotEnchant(player, itemStack));
  }

  @Test
  void testCanEnchant() {
    assertThat(
        "Correct material can be enchanted",
        listener.canNotEnchant(player, itemStack),
        is(false));
  }

  @Test
  void testPrepareItemEnchantInvalid() {
    itemStack.setType(UNENCHANTABLE_MATERIAL);
    var event = new PrepareItemEnchantEvent(
        player,
        player.getOpenInventory(),
        player.getLocation().getBlock(),
        itemStack,
        new EnchantmentOffer[3],
        15);
    assertDoesNotThrow(() -> server.getPluginManager().callEvent(event));
    assertThat(
        "Invalid material does not yield offers",
        event.getOffers(),
        Matchers.arrayContaining(nullValue(), nullValue(), nullValue()));
  }

  @Test
  void testPrepareItemEnchant() {
    player.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 0);
    var event = new PrepareItemEnchantEvent(
        player,
        player.getOpenInventory(),
        player.getLocation().getBlock(),
        itemStack,
        new EnchantmentOffer[3],
        15);
    assertDoesNotThrow(() -> server.getPluginManager().callEvent(event));
    assertThat(
        "Seed yielding results yields offers",
        event.getOffers(),
        Matchers.arrayContaining(notNullValue(), notNullValue(), notNullValue()));
  }

  @Test
  void testEnchantItem() {
    player.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 0);
    var event = new EnchantItemEvent(
        player,
        player.getOpenInventory(),
        player.getLocation().getBlock(),
        itemStack,
        30,
        new HashMap<>(),
        2);
    assertDoesNotThrow(() -> server.getPluginManager().callEvent(event));
    assertThat("Enchantments must not be empty", event.getEnchantsToAdd(), not(anEmptyMap()));
  }

}