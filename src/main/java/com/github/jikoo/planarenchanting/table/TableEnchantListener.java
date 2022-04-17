package com.github.jikoo.planarenchanting.table;

import java.util.Random;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * An abstraction to remove the boilerplate of implementing custom enchantment.
 */
public abstract class TableEnchantListener implements Listener {

  // We specifically want our own random so we can seed it.
  private final @NotNull Random random = new Random();
  private final @NotNull Plugin plugin;

  /**
   * Construct a new {@code TableEnchantListener}.
   *
   * @param plugin the owning plugin
   */
  public TableEnchantListener(@NotNull Plugin plugin) {
    this.plugin = plugin;
    // Set up enchantment seed management.
    EnchantingTable.setUpSeeding(plugin);
  }

  @EventHandler
  private void onPrepareItemEnchant(@NotNull PrepareItemEnchantEvent event) {
    // Ensure item is enchantable.
    if (canNotEnchant(event.getEnchanter(), event.getItem())) {
      return;
    }

    // Seed the random. Button index is 0 for button level generation.
    EnchantingTable.seedRandom(random, plugin, event.getEnchanter(), 0);

    // Calculate levels offered for bookshelf count.
    int[] buttonLevels = EnchantingTable.getButtonLevels(random, event.getEnchantmentBonus());


    for (int buttonIndex = 0; buttonIndex < buttonLevels.length; ++buttonIndex) {
      // Seed random with button index.
      EnchantingTable.seedRandom(random, plugin, event.getEnchanter(), buttonIndex);

      // Generate and set the offer.
      event.getOffers()[buttonIndex] = getTable(event.getEnchanter(), event.getItem())
          .getOffer(random, buttonLevels[buttonIndex]);
    }

    // Force button refresh. This is required for normally unenchantable items.
    EnchantingTable.updateButtons(plugin, event.getEnchanter(), event.getOffers());
  }

  @EventHandler
  private void onEnchantItem(@NotNull EnchantItemEvent event) {
    // Ensure item is enchantable.
    if (canNotEnchant(event.getEnchanter(), event.getItem())) {
      return;
    }

    // Seed the random.
    EnchantingTable.seedRandom(random, plugin, event.getEnchanter(), event.whichButton());

    // Calculate and set enchantments.
    event.getEnchantsToAdd().putAll(
        getTable(event.getEnchanter(), event.getItem())
            .apply(random, event.getExpLevelCost()));
  }

  /**
   * Ensure the enchanter cannot enchant the specified item. By default, this ensures that the item
   * is unstacked, calls {@link #isIneligible(Player, ItemStack)}, and then ensures that the item is
   * not already enchanted.
   *
   * @see #isIneligible(Player, ItemStack)
   * @param player the enchanter
   * @param enchanted the item enchanted
   * @return whether the enchanter is allowed to enchant the item
   */
  protected boolean canNotEnchant(@NotNull Player player, @NotNull ItemStack enchanted) {
    return enchanted.getAmount() != 1
        || isIneligible(player, enchanted)
        || !enchanted.getEnchantments().isEmpty();
  }

  /**
   * Ensure the enchanter cannot enchant the specified item.
   *
   * @param player the enchanter
   * @param enchanted the item enchanted
   * @return whether the enchanter is allowed to enchant the item
   */
  protected abstract boolean isIneligible(@NotNull Player player, @NotNull ItemStack enchanted);

  /**
   * Get the {@link EnchantingTable} instance for the enchanting operation being performed.
   *
   * @param player the enchanter
   * @param enchanted the item enchanted
   * @return the {@code EnchantingTable}
   */
  protected abstract @NotNull EnchantingTable getTable(
      @NotNull Player player,
      @NotNull ItemStack enchanted);

}
