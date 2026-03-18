package com.github.jikoo.planarenchanting.anvil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MetaEnchantmentAccessTest {

  private MockedStatic<Bukkit> bukkit;
  private MetaEnchantmentAccess access;

  @BeforeAll
  void setUp() {
    bukkit = mockStatic();
    bukkit.when(() -> Bukkit.getRegistry(any())).thenAnswer(invocation -> {
      Registry<?> registry = mock(Registry.class);
      if (Enchantment.class.isAssignableFrom(invocation.getArgument(0))) {
        doAnswer(invocation1 -> mock(Enchantment.class)).when(registry).getOrThrow(any());
      }
      return registry;
    });

    access = new MetaEnchantmentAccess();
  }

  @AfterAll
  void tearDown() {
    bukkit.close();
  }

  @Test
  void isBook() {
    ItemStack stack = mock();
    doReturn(Material.ENCHANTED_BOOK).when(stack).getType();
    MetaCachedStack metaStack = mock();
    doReturn(stack).when(metaStack).getItem();

    assertThat("Item is book", access.isBook(metaStack));
  }

  @Test
  void isNotBook() {
    ItemStack stack = mock();
    doReturn(Material.DIRT).when(stack).getType();
    MetaCachedStack metaStack = mock();
    doReturn(stack).when(metaStack).getItem();

    assertThat("Item is not a book", access.isBook(metaStack), is(false));
  }

  @Test
  void getEnchantments() {
    ItemMeta meta = mock();
    Map<Enchantment, Integer> enchants = Map.of(Enchantment.EFFICIENCY, 5);
    doReturn(enchants).when(meta).getEnchants();
    MetaCachedStack metaStack = mock();
    doReturn(meta).when(metaStack).getMeta();

    assertThat("Item enchantments are fetched", access.getEnchantments(metaStack), is(enchants));
  }

  @Test
  void getEnchantmentsStored() {
    EnchantmentStorageMeta meta = mock();
    Map<Enchantment, Integer> enchants = Map.of(Enchantment.EFFICIENCY, 5);
    doReturn(enchants).when(meta).getStoredEnchants();
    MetaCachedStack metaStack = mock();
    doReturn(meta).when(metaStack).getMeta();

    assertThat("Stored enchantments are fetched", access.getEnchantments(metaStack), is(enchants));
  }

  @Test
  void addEnchantments() {
    ItemMeta meta = mock();
    MetaCachedStack metaStack = mock();
    doReturn(meta).when(metaStack).getMeta();

    Map<Enchantment, Integer> enchants = Map.of(Enchantment.EFFICIENCY, 5);

    access.addEnchantments(metaStack, enchants);

    verify(meta, times(enchants.size())).addEnchant(any(), anyInt(), eq(true));
  }

  @Test
  void addEnchantmentsNullMeta() {
    MetaCachedStack metaStack = mock();
    Map<Enchantment, Integer> enchants = Map.of(Enchantment.EFFICIENCY, 5);

    assertDoesNotThrow(() -> access.addEnchantments(metaStack, enchants));
  }

  @Test
  void addEnchantmentsStored() {
    EnchantmentStorageMeta meta = mock();
    MetaCachedStack metaStack = mock();
    doReturn(meta).when(metaStack).getMeta();

    Map<Enchantment, Integer> enchants = Map.of(Enchantment.EFFICIENCY, 5);

    access.addEnchantments(metaStack, enchants);

    verify(meta, times(enchants.size())).addStoredEnchant(any(), anyInt(), eq(true));
  }

}
