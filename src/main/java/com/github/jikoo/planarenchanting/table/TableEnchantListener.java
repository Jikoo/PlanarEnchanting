package com.github.jikoo.planarenchanting.table;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntSupplier;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An abstraction to remove the boilerplate of implementing custom enchantment.
 */
public abstract class TableEnchantListener implements Listener {

  // We specifically want our own random so we can seed it.
  private final @NotNull Random random = new Random();
  private final @NotNull Plugin plugin;
  private final NamespacedKey key;

  /**
   * Construct a new {@code TableEnchantListener}.
   *
   * @param plugin the owning plugin
   */
  protected TableEnchantListener(@NotNull Plugin plugin) {
    this.plugin = plugin;
    this.key = new NamespacedKey(this.plugin, "enchanting_table_seed");
  }

  @EventHandler
  public final void onPrepareItemEnchant(@NotNull PrepareItemEnchantEvent event) {
    // Ensure item is enchantable.
    if (canNotEnchant(event.getEnchanter(), event.getItem())) {
      return;
    }

    // Get the EnchantingTable instance to be used.
    EnchantingTable table = getTable(event.getEnchanter(), event.getItem());
    if (table == null) {
      return;
    }

    // Seed the random. Button index is 0 for button level generation.
    random.setSeed(getSeed(event.getEnchanter(), 0));

    // Calculate levels offered for bookshelf count.
    int[] buttonLevels = EnchantingTable.getButtonLevels(random, event.getEnchantmentBonus());


    for (int buttonIndex = 0; buttonIndex < buttonLevels.length; ++buttonIndex) {
      // Seed random with button index.
      random.setSeed(getSeed(event.getEnchanter(), buttonIndex));

      // Generate and set the offer.
      event.getOffers()[buttonIndex] = table.getOffer(random, buttonLevels[buttonIndex]);
    }

    // Force button refresh. This is required for normally unenchantable items.
    EnchantingTable.updateButtons(plugin, event.getEnchanter(), event.getOffers());
  }

  @EventHandler
  public final void onEnchantItem(@NotNull EnchantItemEvent event) {
    // Ensure item is enchantable.
    if (canNotEnchant(event.getEnchanter(), event.getItem())) {
      return;
    }

    // Get the EnchantingTable instance to be used.
    EnchantingTable table = getTable(event.getEnchanter(), event.getItem());
    if (table == null) {
      return;
    }

    // Seed the random.
    random.setSeed(getSeed(event.getEnchanter(), event.whichButton()));

    // Calculate and set enchantments.
    event.getEnchantsToAdd().putAll(table.apply(random, event.getExpLevelCost()));

    randomizeSeed(event.getEnchanter(), TableEnchantListener::getRandomSeed);
  }

  /**
   * Ensure the enchanter cannot enchant the specified item. By default, this ensures that the item
   * is unstacked, calls {@link #isIneligible(Player, ItemStack)}, and then ensures that the item is
   * not already enchanted.
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
   * Ensure the enchanter cannot enchant the specified item. Anything that is required to construct
   * an {@link EnchantingTable} should  be verified in {@link #getTable(Player, ItemStack)} instead.
   *
   * @see #getTable(Player, ItemStack)
   * @param player the enchanter
   * @param enchanted the item enchanted
   * @return whether the enchanter is allowed to enchant the item
   */
  protected abstract boolean isIneligible(@NotNull Player player, @NotNull ItemStack enchanted);

  /**
   * Get the {@link EnchantingTable} instance for the enchanting operation being performed. If
   * {@code null}, no enchantment will occur.
   *
   * @param player the enchanter
   * @param enchanted the item enchanted
   * @return the {@code EnchantingTable}
   */
  protected abstract @Nullable EnchantingTable getTable(
      @NotNull Player player,
      @NotNull ItemStack enchanted);

  private void randomizeSeed(@NotNull Player player, @NotNull IntSupplier supplier) {
    player.getPersistentDataContainer().remove(key);
    player.setEnchantmentSeed(supplier.getAsInt());
  }

  /**
   * Obtain the enchantment seed from the {@link Player}. Rather than use Minecraft's internal seed
   * (the field's obfuscation is very volatile and there is little benefit because we generate
   * enchantments in a slightly different fashion), this uses a plugin-generated seed.
   *
   * @param player the {@link Player}
   * @param buttonIndex the index of the enchanting button
   * @return the enchantment seed
   */
  private long getSeed(@NotNull Player player, int buttonIndex) {
    return getEnchantmentSeed(player) + buttonIndex;
  }

  /**
   * Obtain the enchantment seed from the {@link Player}. If not present, generates a new seed.
   *
   * @param player the {@link Player}
   * @return the enchantment seed
   */
  private long getEnchantmentSeed(@NotNull Player player) {
    // Use legacy existing seed if available.
    var seed = player.getPersistentDataContainer().get(key, PersistentDataType.LONG);

    if (seed == null) {
      // If legacy seed is not available, use internal seed.
      return player.getEnchantmentSeed();
    }

    return seed;
  }

  /**
   * Get a random seed.
   *
   * @return a random seed
   */
  private static int getRandomSeed() {
    return ThreadLocalRandom.current().nextInt();
  }

}
