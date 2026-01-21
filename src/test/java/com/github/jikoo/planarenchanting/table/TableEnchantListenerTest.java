package com.github.jikoo.planarenchanting.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;

@DisplayName("Feature: Enchant blocks in enchanting tables.")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TableEnchantListenerTest {

  private static final Material ENCHANTABLE_MATERIAL = Material.COAL_ORE;
  private static final Material UNENCHANTABLE_MATERIAL = Material.DIRT;
  private static Enchantment validEnchant;
  private static Collection<Enchantment> toolEnchants;

  private TableEnchantListener listener;
  private Player player;
  private ItemStack itemStack;
  private NamespacedKey key;

  @BeforeAll
  void setUpAll() {
    Server server = ServerMocks.mockServer();

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

    EnchantmentMocks.init();

    validEnchant = Enchantment.EFFICIENCY;
    toolEnchants = List.of(
        Enchantment.EFFICIENCY,
        Enchantment.UNBREAKING,
        Enchantment.FORTUNE,
        Enchantment.SILK_TOUCH
    );
  }

  @BeforeEach
  void setUp() {
    Plugin plugin = mock(Plugin.class);
    doReturn("SampleText").when(plugin).getName();
    doReturn("sampletext").when(plugin).namespace();

    listener = new TableEnchantListener(plugin) {
      private final EnchantingTable table = new EnchantingTable(toolEnchants, Enchantability.STONE);

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
  void testCanNotEnchantEnchanted() {
    itemStack.addUnsafeEnchantment(validEnchant, 10);
    assertThat("Enchanted item cannot be enchanted", listener.canNotEnchant(player, itemStack));
  }

  @Test
  void testCanNotEnchantStack() {
    itemStack.setAmount(2);
    assertThat("Stacked item cannot be enchanted", listener.canNotEnchant(player, itemStack));
  }

  @Test
  void testCanNotEnchantWrongMaterial() {
    itemStack = new ItemStack(UNENCHANTABLE_MATERIAL);
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
    itemStack = new ItemStack(UNENCHANTABLE_MATERIAL);
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

  @DisplayName("Button updates send to user as expected.")
  @Test
  void testSendButtonUpdates() {
    var scheduler = mock(BukkitScheduler.class);
    ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);
    ArgumentCaptor<Long> delayCaptor = ArgumentCaptor.forClass(Long.class);
    BukkitTask bukkitTask = mock(BukkitTask.class);
    when(scheduler.runTaskLater(any(Plugin.class), taskCaptor.capture(), delayCaptor.capture())).thenReturn(bukkitTask);

    var server = Bukkit.getServer();
    when(server.getScheduler()).thenReturn(scheduler);

    var event = prepareEvent(30);
    var offerData = new AtomicReference<EnchantmentOffer[]>();
    doAnswer(invocation -> {
      offerData.set(invocation.getArgument(0));
      return null;
    }).when(event.getView()).setOffers(notNull());

    assertDoesNotThrow(() -> listener.onPrepareItemEnchant(event));
    assertThat("Task was scheduled", delayCaptor.getValue(), is(notNullValue()));
    assertThat("Offer data is not sent immediately", offerData.get(), is(nullValue()));
    assertThat(
        "Seed yielding results yields offers",
        event.getOffers(),
        Matchers.arrayContaining(notNullValue(), notNullValue(), notNullValue()));

    EnchantmentOffer[] offers = new EnchantmentOffer[] {
        new EnchantmentOffer(Enchantment.EFFICIENCY, 5, 1),
        new EnchantmentOffer(Enchantment.UNBREAKING, 20, 2),
        new EnchantmentOffer(Enchantment.SILK_TOUCH, 30, 3)
    };
    System.arraycopy(offers, 0, event.getOffers(), 0, offers.length);

    assertThat("Delay is 1 tick", delayCaptor.getValue(), is(1L));

    Runnable task = taskCaptor.getValue();

    assertDoesNotThrow(task::run);
    assertThat("Offer data is sent", offerData.get(), is(arrayContaining(offers)));
  }

  @Contract("_ -> new")
  private @NotNull PrepareItemEnchantEvent prepareEvent(int bonus) {
    return new PrepareItemEnchantEvent(
        player,
        mock(),
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
        Enchantment.UNBREAKING,
        0,
        buttonIndex);
  }

}