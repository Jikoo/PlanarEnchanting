package com.github.jikoo.planarenchanting.anvil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.github.jikoo.planarenchanting.util.mock.ServerMocks;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class ComponentEnchantmentAccessTest {

  private ComponentEnchantmentAccess access;

  @BeforeAll
  void setUp() {
    // DataComponentTypes are fetched from the server registry.
    ServerMocks.mockServer();
    // Touch to initialize.
    DataComponentTypes.ENCHANTMENTS.key();
    access = new ComponentEnchantmentAccess();
  }

  @Test
  void isBook() {
    ItemStack stack = mock();
    doReturn(Material.ENCHANTED_BOOK).when(stack).getType();

    assertThat("Item is book", access.isBook(stack));
  }

  @Test
  void isNotBook() {
    ItemStack stack = mock();
    doReturn(Material.DIRT).when(stack).getType();

    assertThat("Item is not a book", access.isBook(stack), is(false));
  }

  @Test
  void getEnchantments() {
    ItemStack stack = mock();
    ItemEnchantments enchantments = mock(ItemEnchantments.class);
    doReturn(enchantments).when(stack).getData(DataComponentTypes.ENCHANTMENTS);
    Map<Enchantment, Integer> enchants = Map.of(Enchantment.EFFICIENCY, 5);
    doReturn(enchants).when(enchantments).enchantments();

    assertThat("Item enchantments are fetched", access.getEnchantments(stack), is(enchants));
  }

  @Test
  void getEnchantmentsStored() {
    ItemStack stack = mock();
    doReturn(Material.ENCHANTED_BOOK).when(stack).getType();
    ItemEnchantments enchantments = mock(ItemEnchantments.class);
    doReturn(enchantments).when(stack).getData(DataComponentTypes.STORED_ENCHANTMENTS);
    Map<Enchantment, Integer> enchants = Map.of(Enchantment.EFFICIENCY, 5);
    doReturn(enchants).when(enchantments).enchantments();

    assertThat("Stored enchantments are fetched", access.getEnchantments(stack), is(enchants));
  }

  @Test
  void addEnchantments() {
    ItemStack stack = mock();
    access.addEnchantments(stack, Map.of(Enchantment.EFFICIENCY, 5));
    verify(stack).setData(eq(DataComponentTypes.ENCHANTMENTS), any(ItemEnchantments.class));
  }

  @Test
  void addEnchantmentsStored() {
    ItemStack stack = mock();
    doReturn(Material.ENCHANTED_BOOK).when(stack).getType();
    access.addEnchantments(stack, Map.of(Enchantment.EFFICIENCY, 5));
    verify(stack).setData(eq(DataComponentTypes.STORED_ENCHANTMENTS), any(ItemEnchantments.class));
  }

}
