package com.github.jikoo.planarenchanting.enchant;

import com.github.jikoo.planarwrappers.util.WeightedRandom;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;

public class EnchantData implements WeightedRandom.Choice {

  private static final Map<Enchantment, EnchantData> ENCHANT_DATA = new HashMap<>();

  static {
    IntUnaryOperator modLootBonus = modLvl(15, 9);
    IntUnaryOperator modLootMax = level -> modLootBonus.applyAsInt(level) + 50;
    addLoot(Enchantment.LOOT_BONUS_MOBS, modLootBonus, modLootMax);
    addLoot(Enchantment.LOOT_BONUS_BLOCKS, modLootBonus, modLootMax);
    addLoot(Enchantment.LUCK, modLootBonus, modLootMax);
    addLoot(Enchantment.LURE, modLootBonus, modLootMax);

    addProtection(Enchantment.PROTECTION_ENVIRONMENTAL, EnchantRarity.COMMON, 1, 11);
    addProtection(Enchantment.PROTECTION_FIRE, EnchantRarity.UNCOMMON, 10, 8);
    addProtection(Enchantment.PROTECTION_FALL, EnchantRarity.UNCOMMON, 5, 6);
    addProtection(Enchantment.PROTECTION_EXPLOSIONS, EnchantRarity.RARE, 5, 8);
    addProtection(Enchantment.PROTECTION_PROJECTILE, EnchantRarity.UNCOMMON, 3, 6);

    IntUnaryOperator lvlTimes10 = level -> level * 10;

    add(Enchantment.OXYGEN, EnchantRarity.RARE, lvlTimes10, 30);
    add(Enchantment.WATER_WORKER, EnchantRarity.RARE, flat(1), 40);
    add(Enchantment.THORNS, EnchantRarity.VERY_RARE, modLvl(10, 20));
    add(Enchantment.DEPTH_STRIDER, EnchantRarity.RARE, lvlTimes10, 15);
    add(Enchantment.FROST_WALKER, EnchantRarity.RARE, lvlTimes10, 15);
    add(Enchantment.SOUL_SPEED, EnchantRarity.VERY_RARE, lvlTimes10, 15);

    add(Enchantment.DAMAGE_ALL, EnchantRarity.COMMON, modLvl(1, 11), 20);
    add(Enchantment.DAMAGE_UNDEAD, EnchantRarity.UNCOMMON, modLvl(5, 8), 20);
    add(Enchantment.DAMAGE_ARTHROPODS, EnchantRarity.UNCOMMON, modLvl(5, 8), 20);
    add(Enchantment.KNOCKBACK, EnchantRarity.UNCOMMON, modLvl(5, 20));
    add(Enchantment.FIRE_ASPECT, EnchantRarity.RARE, modLvl(10, 20));
    add(Enchantment.SWEEPING_EDGE, EnchantRarity.RARE, modLvl(5, 9), 15);

    add(Enchantment.DIG_SPEED, EnchantRarity.COMMON, modLvl(1, 10));
    add(Enchantment.SILK_TOUCH, EnchantRarity.VERY_RARE, flat(15), modLvl(61, 10));
    add(Enchantment.DURABILITY, EnchantRarity.UNCOMMON, modLvl(5, 8));

    IntUnaryOperator flat25 = flat(25);
    IntUnaryOperator flat50 = flat(50);

    add(Enchantment.VANISHING_CURSE, EnchantRarity.VERY_RARE, flat25, flat50);
    add(Enchantment.BINDING_CURSE, EnchantRarity.VERY_RARE, flat25, flat50);

    IntUnaryOperator flat20 = flat(20);

    add(Enchantment.ARROW_DAMAGE, EnchantRarity.COMMON, modLvl(1, 10), 15);
    add(Enchantment.ARROW_KNOCKBACK, EnchantRarity.RARE, modLvl(12, 20), 25);
    add(Enchantment.ARROW_FIRE, EnchantRarity.RARE, flat20, flat50);
    add(Enchantment.ARROW_INFINITE, EnchantRarity.VERY_RARE, flat20, flat50);
    add(Enchantment.LOYALTY, EnchantRarity.UNCOMMON, modLvl(12, 7), flat50);
    add(Enchantment.IMPALING, EnchantRarity.RARE, modLvl(1, 8), 20);
    add(Enchantment.RIPTIDE, EnchantRarity.RARE, modLvl(17, 7), flat50);
    add(Enchantment.CHANNELING, EnchantRarity.VERY_RARE, flat25, flat50);
    add(Enchantment.MULTISHOT, EnchantRarity.RARE, flat20, flat50);
    add(Enchantment.QUICK_CHARGE, EnchantRarity.UNCOMMON, level -> 12 + (level - 1) * 20, flat50);
    add(Enchantment.PIERCING, EnchantRarity.COMMON, modLvl(1, 10), flat50);
    add(Enchantment.MENDING, EnchantRarity.RARE, level -> level * 25);
  }

  private static @NotNull IntUnaryOperator modLvl(int base, int levelMod) {
    return level -> base + (level - 1) * levelMod;
  }

  private static @NotNull IntUnaryOperator flat(int value) {
    return integer -> value;
  }

  private static void add(
      @NotNull Enchantment enchantment,
      @NotNull EnchantRarity enchantRarity,
      @NotNull IntUnaryOperator min,
      @NotNull IntUnaryOperator max) {
    EnchantData data = new EnchantData(enchantment, enchantRarity, min, max);
    ENCHANT_DATA.put(data.getEnchantment(), data);
  }

  private static void add(
      @NotNull Enchantment enchantment,
      @NotNull EnchantRarity enchantRarity,
      @NotNull IntUnaryOperator min,
      int maxMod) {
    add(enchantment, enchantRarity, min, level -> min.applyAsInt(level) + maxMod);
  }

  private static void add(
      @NotNull Enchantment enchantment,
      @NotNull EnchantRarity enchantRarity,
      @NotNull IntUnaryOperator min) {
    add(enchantment, enchantRarity, min, 50);
  }

  private static void addProtection(
      @NotNull Enchantment enchantment,
      @NotNull EnchantRarity enchantRarity,
      int base,
      int levelMod) {
    add(enchantment, enchantRarity, modLvl(base, levelMod), levelMod);
  }

  private static void addLoot(
      @NotNull Enchantment enchantment,
      @NotNull IntUnaryOperator min,
      @NotNull IntUnaryOperator max) {
    add(enchantment, EnchantRarity.RARE, min, max);
  }

  @TestOnly
  static boolean isPresent(@NotNull Enchantment enchantment) {
    return ENCHANT_DATA.containsKey(enchantment);
  }

  public static EnchantData of(@NotNull Enchantment enchantment) {
    return ENCHANT_DATA.computeIfAbsent(enchantment, EnchantData::new);
  }

  private final @NotNull Enchantment enchantment;
  private final @NotNull EnchantRarity enchantRarity;
  private final @NotNull IntUnaryOperator minCost;
  private final @NotNull IntUnaryOperator maxCost;

  private EnchantData(@NotNull Enchantment enchantment) {
    this(enchantment, EnchantDataReflection.getRarity(enchantment),
        EnchantDataReflection.getMinCost(enchantment),
        EnchantDataReflection.getMaxCost(enchantment));
  }

  private EnchantData(
      @NotNull Enchantment enchantment,
      @NotNull EnchantRarity enchantRarity,
      @NotNull IntUnaryOperator minEnchantQuality,
      @NotNull IntUnaryOperator maxEnchantQuality) {
    this.enchantment = enchantment;
    this.enchantRarity = enchantRarity;
    this.minCost = minEnchantQuality;
    this.maxCost = maxEnchantQuality;
  }

  public @NotNull Enchantment getEnchantment() {
    return this.enchantment;
  }

  public @NotNull EnchantRarity getRarity() {
    return this.enchantRarity;
  }

  @Override
  public int getWeight() {
    return this.getRarity().getWeight();
  }

  public int getMinCost(int level) {
    return this.minCost.applyAsInt(level);
  }

  public int getMaxCost(int level) {
    return this.maxCost.applyAsInt(level);
  }

}
