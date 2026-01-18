package com.github.jikoo.planarenchanting.util.mock.inventory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public enum ItemStackMocks {
  ;

  public static ItemStack newItemMock(@NotNull Material material) {
    return newItemMock(Objects.requireNonNull(material.asItemType()), 1);
  }

  public static ItemStack newItemMock(@NotNull ItemType type, int amount) {
    ItemStack stack = mock();

    doReturn(type.asMaterial()).when(stack).getType();

    // Amount get/set.
    AtomicInteger amt = new AtomicInteger(amount);
    doAnswer(invocation -> amt.get()).when(stack).getAmount();
    doAnswer(invocation -> {
      amt.set(invocation.getArgument(0));
      return null;
    }).when(stack).setAmount(anyInt());

    // Item meta.
    AtomicReference<ItemMeta> meta = new AtomicReference<>();
    doAnswer(invocation -> {
      ItemMeta existing = meta.get();
      if (existing != null) {
        return existing.clone();
      }
      return Bukkit.getItemFactory().getItemMeta(stack.getType());
    }).when(stack).getItemMeta();
    doAnswer(invocation -> {
      ItemMeta newMeta = invocation.getArgument(0);
      meta.set(newMeta != null ? newMeta.clone() : null);
      return null;
    }).when(stack).setItemMeta(any());
    doAnswer(invocation -> meta.get() != null).when(stack).hasItemMeta();

    // Item cloning.
    doAnswer(invocation -> {
      ItemStack clone = newItemMock(type, amount);
      clone.setItemMeta(meta.get());
      return clone;
    }).when(stack).clone();

    // Item similarity. Note that the only thing we track other the meta is the count.
    doAnswer(invocation -> {
      ItemStack other = invocation.getArgument(0);
      if (other == null || stack.hasItemMeta() != other.hasItemMeta()) {
        return false;
      }
      return Bukkit.getItemFactory().equals(meta.get(), other.getItemMeta());
    }).when(stack).isSimilar(any());

    return stack;
  }

}
