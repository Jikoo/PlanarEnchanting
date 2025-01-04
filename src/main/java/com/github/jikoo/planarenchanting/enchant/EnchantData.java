package com.github.jikoo.planarenchanting.enchant;

import com.github.jikoo.planarenchanting.util.ItemUtil;
import com.github.jikoo.planarwrappers.util.WeightedRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

public class EnchantData implements WeightedRandom.Choice {

  private static final Map<Enchantment, EnchantData> ENCHANT_DATA = new HashMap<>();

  static {
    // NMSREF net.minecraft.world.item.enchantment.Enchantments
    // Armor
    add(Enchantment.PROTECTION, Tag.ITEMS_ENCHANTABLE_ARMOR, 10, 1, perLvl(1, 11), perLvl(12, 11));
    add(Enchantment.FIRE_PROTECTION, Tag.ITEMS_ENCHANTABLE_ARMOR, 5, 2, perLvl(10, 8), perLvl(18, 8));
    add(Enchantment.FEATHER_FALLING, Tag.ITEMS_ENCHANTABLE_FOOT_ARMOR, 5, 2, perLvl(5, 6), perLvl(11, 6));
    add(Enchantment.BLAST_PROTECTION, Tag.ITEMS_ENCHANTABLE_ARMOR, 2, 4, perLvl(1, 11), perLvl(12, 11));
    add(Enchantment.PROJECTILE_PROTECTION, Tag.ITEMS_ENCHANTABLE_ARMOR, 5, 2, perLvl(3, 6), perLvl(9, 6));
    add(Enchantment.RESPIRATION, Tag.ITEMS_ENCHANTABLE_HEAD_ARMOR, 2, 4, perLvl(10, 10), perLvl(40, 10));
    add(Enchantment.AQUA_AFFINITY, Tag.ITEMS_ENCHANTABLE_HEAD_ARMOR, 2, 4, flat(1), flat(41));
    add(Enchantment.THORNS, Tag.ITEMS_ENCHANTABLE_CHEST_ARMOR, 1, 8, perLvl(10, 20), perLvl(60, 20));
    add(Enchantment.DEPTH_STRIDER, Tag.ITEMS_ENCHANTABLE_FOOT_ARMOR, 2, 4, perLvl(10, 10), perLvl(25, 10));
    add(Enchantment.FROST_WALKER, ItemUtil.TAG_EMPTY, Tag.ITEMS_ENCHANTABLE_FOOT_ARMOR, 2, 4, perLvl(10, 10), perLvl(25, 10));
    add(Enchantment.BINDING_CURSE, ItemUtil.TAG_EMPTY, Tag.ITEMS_ENCHANTABLE_EQUIPPABLE, 1, 8, flat(25), flat(50));
    add(Enchantment.SOUL_SPEED, ItemUtil.TAG_EMPTY, Tag.ITEMS_ENCHANTABLE_FOOT_ARMOR, 1, 8, perLvl(10, 10), perLvl(25, 10));
    add(Enchantment.SWIFT_SNEAK, ItemUtil.TAG_EMPTY, Tag.ITEMS_ENCHANTABLE_LEG_ARMOR, 1, 8, perLvl(25, 25), perLvl(75, 25));
    // Melee weapon
    add(Enchantment.SHARPNESS, Tag.ITEMS_ENCHANTABLE_SHARP_WEAPON, Tag.ITEMS_ENCHANTABLE_SWORD, 10, 1, perLvl(1, 11), perLvl(21, 11));
    add(Enchantment.SMITE, Tag.ITEMS_ENCHANTABLE_WEAPON, Tag.ITEMS_ENCHANTABLE_SWORD, 5, 2, perLvl(5, 8), perLvl(25, 8));
    add(Enchantment.BANE_OF_ARTHROPODS, Tag.ITEMS_ENCHANTABLE_WEAPON, Tag.ITEMS_ENCHANTABLE_SWORD, 5, 2, perLvl(5, 8), perLvl(25, 8));
    add(Enchantment.KNOCKBACK, Tag.ITEMS_ENCHANTABLE_SWORD, 5, 2, perLvl(5, 20), perLvl(55, 20));
    add(Enchantment.FIRE_ASPECT, Tag.ITEMS_ENCHANTABLE_FIRE_ASPECT, 2, 4, perLvl(10, 20), perLvl(60, 20));
    add(Enchantment.LOOTING, Tag.ITEMS_ENCHANTABLE_SWORD, 2, 4, perLvl(15, 9), perLvl(65, 9));
    add(Enchantment.SWEEPING_EDGE, Tag.ITEMS_ENCHANTABLE_SWORD, 2, 4, perLvl(5, 9), perLvl(20, 9));
    // Tool
    add(Enchantment.EFFICIENCY, Tag.ITEMS_ENCHANTABLE_MINING, 10, 1, perLvl(1, 10), perLvl(51, 10));
    add(Enchantment.SILK_TOUCH, Tag.ITEMS_ENCHANTABLE_MINING_LOOT, 1, 8, flat(15), flat(65));
    add(Enchantment.UNBREAKING, Tag.ITEMS_ENCHANTABLE_DURABILITY, 5, 2, perLvl(5, 8), perLvl(55, 8));
    add(Enchantment.FORTUNE, Tag.ITEMS_ENCHANTABLE_MINING_LOOT, 2, 4, perLvl(15, 9), perLvl(65, 9));
    // Bow
    add(Enchantment.POWER, Tag.ITEMS_ENCHANTABLE_BOW, 10, 1, perLvl(1, 10), perLvl(16, 10));
    add(Enchantment.PUNCH, Tag.ITEMS_ENCHANTABLE_BOW, 2, 4, perLvl(12, 20), perLvl(37, 20));
    add(Enchantment.FLAME, Tag.ITEMS_ENCHANTABLE_BOW, 2, 4, flat(20), flat(50));
    add(Enchantment.INFINITY, Tag.ITEMS_ENCHANTABLE_BOW, 1, 8, flat(20), flat(50));
    // Fishing rod
    add(Enchantment.LUCK_OF_THE_SEA, Tag.ITEMS_ENCHANTABLE_FISHING, 2, 4, perLvl(15, 9), perLvl(65, 9));
    add(Enchantment.LURE, Tag.ITEMS_ENCHANTABLE_FISHING, 2, 4, perLvl(15, 9), perLvl(65, 9));
    // Trident
    add(Enchantment.LOYALTY, Tag.ITEMS_ENCHANTABLE_TRIDENT, 5, 2, perLvl(12, 7), flat(50));
    add(Enchantment.IMPALING, Tag.ITEMS_ENCHANTABLE_TRIDENT, 2, 4, perLvl(1, 8), perLvl(21, 8));
    add(Enchantment.RIPTIDE, Tag.ITEMS_ENCHANTABLE_TRIDENT, 2, 4, perLvl(17, 7), flat(50));
    add(Enchantment.CHANNELING, Tag.ITEMS_ENCHANTABLE_TRIDENT, 1, 8, flat(25), flat(50));
    // Crossbow
    add(Enchantment.MULTISHOT, Tag.ITEMS_ENCHANTABLE_CROSSBOW, 2, 4, flat(20), flat(50));
    add(Enchantment.QUICK_CHARGE, Tag.ITEMS_ENCHANTABLE_CROSSBOW, 5, 2, perLvl(12, 20), flat(50));
    add(Enchantment.PIERCING, Tag.ITEMS_ENCHANTABLE_CROSSBOW, 10, 1, perLvl(1, 10), flat(50));
    // Mace
    add(Enchantment.DENSITY, Tag.ITEMS_ENCHANTABLE_MACE, 5, 2, perLvl(5, 8), perLvl(25, 8));
    add(Enchantment.BREACH, Tag.ITEMS_ENCHANTABLE_MACE, 2, 4, perLvl(15, 9), perLvl(65, 9));
    add(Enchantment.WIND_BURST, ItemUtil.TAG_EMPTY, Tag.ITEMS_ENCHANTABLE_MACE, 2, 4, perLvl(15, 9), perLvl(65, 9));
    // General
    add(Enchantment.MENDING, ItemUtil.TAG_EMPTY, Tag.ITEMS_ENCHANTABLE_DURABILITY, 2, 4, perLvl(25, 25), perLvl(75, 25));
    add(Enchantment.VANISHING_CURSE, ItemUtil.TAG_EMPTY, Tag.ITEMS_ENCHANTABLE_VANISHING, 1, 8, flat(25), flat(50));
  }

  private static void add(
      @NotNull Enchantment enchant,
      @Nullable Tag<Material> primaryItems,
      @NotNull Tag<Material> secondaryItems,
      int weight,
      int anvilCost,
      @NotNull IntUnaryOperator minEnchantQuality,
      @NotNull IntUnaryOperator maxEnchantQuality) {
    ENCHANT_DATA.put(enchant, new EnchantData(enchant, primaryItems, secondaryItems, weight, anvilCost, minEnchantQuality, maxEnchantQuality));
  }

  private static void add(
      @NotNull Enchantment enchant,
      @NotNull Tag<Material> items,
      int weight,
      int anvilCost,
      @NotNull IntUnaryOperator minEnchantQuality,
      @NotNull IntUnaryOperator maxEnchantQuality) {
    ENCHANT_DATA.put(enchant, new EnchantData(enchant, null, items, weight, anvilCost, minEnchantQuality, maxEnchantQuality));
  }

  private static @NotNull IntUnaryOperator perLvl(int base, int perLevel) {
    return level -> base + (level - 1) * perLevel;
  }

  private static @NotNull IntUnaryOperator flat(int value) {
    return integer -> value;
  }

  @TestOnly
  static boolean isPresent(@NotNull Enchantment enchantment) {
    return ENCHANT_DATA.containsKey(enchantment);
  }

  public static EnchantData of(@NotNull Enchantment enchantment) {
    return ENCHANT_DATA.computeIfAbsent(enchantment, EnchantData::new);
  }

  private final @NotNull Enchantment enchantment;
  private final @Nullable Tag<Material> primaryItems;
  private final @NotNull Tag<Material> secondaryItems;
  private final int weight;
  private final int anvilCost;
  private final @NotNull IntUnaryOperator minCost;
  private final @NotNull IntUnaryOperator maxCost;

  private EnchantData(@NotNull Enchantment enchantment) {
    this(enchantment,
        EnchantDataPaper.isTreasure(enchantment) ? ItemUtil.TAG_EMPTY : EnchantDataPaper.getPrimaryItems(enchantment),
        EnchantDataPaper.getSecondaryItems(enchantment),
        EnchantDataPaper.getWeight(enchantment),
        EnchantDataPaper.getAnvilCost(enchantment),
        EnchantDataPaper.getMinCost(enchantment),
        EnchantDataPaper.getMaxCost(enchantment));
  }

  private EnchantData(
      @NotNull Enchantment enchantment,
      @Nullable Tag<Material> primaryItems,
      @NotNull Tag<Material> secondaryItems,
      int weight,
      int anvilCost,
      @NotNull IntUnaryOperator minEnchantQuality,
      @NotNull IntUnaryOperator maxEnchantQuality) {
    this.enchantment = enchantment;
    this.primaryItems = primaryItems;
    this.secondaryItems = secondaryItems;
    this.weight = weight;
    this.anvilCost = anvilCost;
    this.minCost = minEnchantQuality;
    this.maxCost = maxEnchantQuality;
  }

  public @NotNull Enchantment getEnchantment() {
    return enchantment;
  }

  /**
   * Get a {@link Tag} containing the items this enchantment is intended to apply to from an enchanting table.
   *
   * <p>Note that this varies from vanilla for treasure enchantments! In vanilla, treasure enchants
   * (for which there is no Bukkit Tag) do not define an empty tag for their enchanting target; they
   * allow it to fall through to the anvil-applicable tag like other enchantments.<br>
   * PlanarEnchanting instead provides an empty primary items tag if an enchantment is treasure.</p>
   *
   * @return the corresponding tag
   */
  public @NotNull Tag<Material> getPrimaryItems() {
    return primaryItems == null ? secondaryItems : primaryItems;
  }

  public @NotNull Tag<Material> getSecondaryItems() {
    return secondaryItems;
  }

  @Override
  public int getWeight() {
    return this.weight;
  }

  public int getAnvilCost() {
    return this.anvilCost;
  }

  public int getMinCost(int level) {
    return this.minCost.applyAsInt(level);
  }

  public int getMaxCost(int level) {
    return this.maxCost.applyAsInt(level);
  }

}
