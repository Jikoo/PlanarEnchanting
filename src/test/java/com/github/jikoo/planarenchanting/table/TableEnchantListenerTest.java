package com.github.jikoo.planarenchanting.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.jikoo.planarenchanting.util.mock.ServerMocks;
import com.github.jikoo.planarenchanting.util.mock.enchantments.EnchantmentMocks;
import com.github.jikoo.planarenchanting.util.mock.inventory.ItemFactoryMocks;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.scheduler.BukkitScheduler;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
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

  private Server server;
  private Plugin plugin;
  private TableEnchantListener listener;
  private Player player;
  private ItemStack itemStack;
  private NamespacedKey key;

  @BeforeAll
  void setUpAll() {
    EnchantmentMocks.init();

    server = ServerMocks.mockServer();

    var factory = ItemFactoryMocks.mockFactory();
    when(server.getItemFactory()).thenReturn(factory);

    var scheduler = mock(BukkitScheduler.class);
    // Immediately run tasks when called.
    when(scheduler.runTaskLater(any(Plugin.class), any(Runnable.class), anyLong()))
        .thenAnswer(invocation -> {
          invocation.getArgument(1, Runnable.class).run();
          return null;
        });
    when(server.getScheduler()).thenReturn(scheduler);

    Bukkit.setServer(server);
  }

  @BeforeEach
  void setUp() {
    plugin = mock(Plugin.class);
    when(plugin.getName()).thenReturn("SampleText");

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

    var pdc = mock(PersistentDataContainer.class);
    AtomicReference<Long> value = new AtomicReference<>(null);
    when(pdc.get(key, PersistentDataType.LONG)).thenAnswer(invocation -> value.get());
    doAnswer(invocation -> {
      value.set(invocation.getArgument(2));
      return null;
    }).when(pdc).set(eq(key), eq(PersistentDataType.LONG), anyLong());
    doAnswer(invocation -> {
      value.set(null);
      return null;
    }).when(pdc).remove(key);

    player = mock(Player.class);
    when(player.getPersistentDataContainer()).thenReturn(pdc);
    var location = mock(Location.class);
    when(player.getLocation()).thenReturn(location);
    AtomicInteger enchantSeed = new AtomicInteger(0);
    when(player.getEnchantmentSeed()).thenAnswer(invocation -> enchantSeed.get());
    doAnswer(invocation -> {
      // Input number is random - ignore and just increment.
      enchantSeed.incrementAndGet();
      return null;
    }).when(player).setEnchantmentSeed(anyInt());

    itemStack = new ItemStack(ENCHANTABLE_MATERIAL);
    key = new NamespacedKey(plugin, "enchanting_table_seed");
  }

  @Test
  void testEventRegistration() {
    assertThat(
        "No handler for PrepareItemEnchantEvent is registered",
        PrepareItemEnchantEvent.getHandlerList().getRegisteredListeners(),
        is(arrayWithSize(0)));
    assertThat(
        "No handler for EnchantItemEvent is registered",
        EnchantItemEvent.getHandlerList().getRegisteredListeners(),
        is(arrayWithSize(0)));

    // Plugin must be enabled to register events
    when(plugin.isEnabled()).thenReturn(true);
    var loader = new JavaPluginLoader(server);
    when(plugin.getPluginLoader()).thenReturn(loader);
    var description = new PluginDescriptionFile(plugin.getName(), "1.2.3", "cool.beans");
    when(plugin.getDescription()).thenReturn(description);

    var manager = new SimplePluginManager(server, new SimpleCommandMap(server));
    // Plugin manager must be set so that event registration can check if timings are enabled.
    when(server.getPluginManager()).thenReturn(manager);

    assertDoesNotThrow(() -> manager.registerEvents(listener, plugin));

    // Unset plugin manager post-use to not modify server for future tests.
    when(server.getPluginManager()).thenReturn(null);

    assertThat(
        "Handler for PrepareItemEnchantEvent is registered",
        PrepareItemEnchantEvent.getHandlerList().getRegisteredListeners(),
        is(arrayWithSize(1)));
    assertThat(
        "Handler for EnchantItemEvent is registered",
        EnchantItemEvent.getHandlerList().getRegisteredListeners(),
        is(arrayWithSize(1)));
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
    var event = prepareEvent(15);
    assertDoesNotThrow(() -> listener.onPrepareItemEnchant(event));
    assertThat(
        "Invalid material does not yield offers",
        event.getOffers(),
        Matchers.arrayContaining(nullValue(), nullValue(), nullValue()));
  }

  @Test
  void testPrepareItemEnchant() {
    var event = prepareEvent(30);
    assertDoesNotThrow(() -> listener.onPrepareItemEnchant(event));
    assertThat(
        "Seed yielding results yields offers",
        event.getOffers(),
        Matchers.arrayContaining(notNullValue(), notNullValue(), notNullValue()));
  }

  @Test
  void testEnchantItem() {
    var event = enchantEvent(30, 2);
    assertDoesNotThrow(() -> listener.onEnchantItem(event));
    assertThat("Enchantments must not be empty", event.getEnchantsToAdd(), not(anEmptyMap()));
  }

  @Test
  void testSeedChangedPostEnchant() {
    int seed = player.getEnchantmentSeed();
    assertDoesNotThrow(() -> listener.onEnchantItem(enchantEvent(1, 0)));

    assertThat("Seed is changed", player.getEnchantmentSeed(), is(not(seed)));
  }

  @Test
  void testLegacySeedUnsetPostEnchant() {
    player.getPersistentDataContainer().set(key, PersistentDataType.LONG, 0L);
    assertThat(
        "Seed is set",
        player.getPersistentDataContainer().get(key, PersistentDataType.LONG),
        is(notNullValue()));

    assertDoesNotThrow(() -> listener.onEnchantItem(enchantEvent(1, 0)));

    assertThat(
        "Seed is unset",
        player.getPersistentDataContainer().get(key, PersistentDataType.LONG),
        is(nullValue()));
  }

  @Contract("_ -> new")
  private @NotNull PrepareItemEnchantEvent prepareEvent(int bonus) {
    return new PrepareItemEnchantEvent(
        player,
        player.getOpenInventory(),
        player.getLocation().getBlock(),
        itemStack,
        new EnchantmentOffer[3],
        bonus);
  }

  @Contract("_, _ -> new")
  private @NotNull EnchantItemEvent enchantEvent(int level, int buttonIndex) {
    return new EnchantItemEvent(
        player,
        player.getOpenInventory(),
        player.getLocation().getBlock(),
        itemStack,
        level,
        new HashMap<>(),
        buttonIndex);
  }

}