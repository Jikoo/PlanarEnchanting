package com.github.jikoo.planarenchanting.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A wrapper for an {@link ItemStack} with a cached {@link ItemMeta}. This is used primarily to
 * prevent redundant object creation while repeatedly manipulating items in different ways -
 * copying details while reobtaining metadata can become relatively costly.
 */
public class MetaCachedStack {

  private final @NotNull ItemStack itemStack;
  private final @NotNull CachedValue<@Nullable ItemMeta> metaCache;

  /**
   * Wrap an {@link ItemStack}. If null, the constant {@link ItemUtil#AIR} will be used instead.
   *
   * @param item the item to wrap
   */
  public MetaCachedStack(@Nullable ItemStack item) {
    this.itemStack = item == null ? ItemUtil.AIR : item;
    this.metaCache = new CachedValue<>(this.itemStack::getItemMeta);
  }

  /**
   * Get the wrapped {@link ItemStack}. Changes to its meta will not affect the cached meta if it
   * has already been accessed!
   *
   * @return the wrapped item
   */
  public @NotNull ItemStack getItem() {
    return this.itemStack;
  }

  /**
   * Get the {@link ItemMeta} associated with the item. This is cached after the first access.
   *
   * @return the item's metadata
   */
  public @Nullable ItemMeta getMeta() {
    return this.metaCache.get();
  }

}
