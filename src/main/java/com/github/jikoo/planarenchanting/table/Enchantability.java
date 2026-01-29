package com.github.jikoo.planarenchanting.table;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

/**
 * Representation of materials' enchantability.
 *
 * <p>Note that setting enchantability too high may actually cause some enchantments to be available
 * less frequently. For example, {@code minecraft:infinity} is available only when the effective
 * enchanting level after modifiers is between two hardcoded values. Higher enchantability
 * increases the calculated end number of a randomized bonus range starting at {@code 0}. The higher
 * the max, the higher the average. If the total number including the bonus exceeds the maximum, you
 * may see very rare enchantments generate at low levels or higher than intended enchantment rarity.
 */
public record Enchantability(@Range(from = 1, to = Integer.MAX_VALUE) int value) {

  public static final Enchantability ARMADILLO;
  public static final Enchantability LEATHER;
  public static final Enchantability CHAIN;
  public static final Enchantability COPPER_ARMOR;
  public static final Enchantability IRON_ARMOR;
  public static final Enchantability GOLD_ARMOR;
  public static final Enchantability DIAMOND_ARMOR;
  public static final Enchantability NETHERITE_ARMOR;
  public static final Enchantability TURTLE;
  public static final Enchantability WOOD;
  public static final Enchantability STONE;
  public static final Enchantability COPPER_TOOL;
  public static final Enchantability IRON_TOOL;
  public static final Enchantability GOLD_TOOL;
  public static final Enchantability DIAMOND_TOOL;
  public static final Enchantability NETHERITE_TOOL;
  public static final Enchantability BOOK;
  public static final Enchantability TRIDENT;
  public static final Enchantability MACE;

  /**
   * Get the {@code Enchantability} of a {@link Material}. Will return {@code null} if not
   * enchantable in an enchanting table.
   *
   * <p>Note that {@link Material#BOOK} and {@link Material#ENCHANTED_BOOK} are both listed as
   * enchantable. This is a special case designed to ease the process for people seeking to handle
   * book enchantment, but technically enchanted books are not enchantable.
   *
   * @param material the {@code Material}
   * @return the {@code Enchantability} if enchantable
   */
  public static @Nullable Enchantability forMaterial(@NotNull Material material) {
    return BY_KEY.get(material.getKey());
  }

  /**
   * Get the {@code Enchantability} of an {@link ItemType}. Will return {@code null} if not
   * enchantable in an enchanting table.
   *
   * <p>Note that {@link ItemType#BOOK} and {@link ItemType#ENCHANTED_BOOK} are both listed as
   * enchantable. This is a special case designed to ease the process for people seeking to handle
   * book enchantment, but technically enchanted books are not enchantable.
   *
   * @param type the {@code ItemType}
   * @return the {@code Enchantability} if enchantable
   */
  public static @Nullable Enchantability forType(@NotNull ItemType type) {
    return BY_KEY.get(type.getKey());
  }

  private static final Map<NamespacedKey, Enchantability> BY_KEY = new HashMap<>();

  static {
    // See net.minecraft.world.item.equipment.ArmorMaterials
    String[] armor = new String[] { "_helmet", "_chestplate", "_leggings", "_boots" };
    LEATHER = addMaterials("leather", armor, 15);
    COPPER_ARMOR = addMaterials("copper", armor, 8);
    CHAIN = addMaterials("chainmail", armor, 12);
    IRON_ARMOR = addMaterials("iron", armor, 9);
    GOLD_ARMOR = addMaterials("golden", armor, 25);
    DIAMOND_ARMOR = addMaterials("diamond", armor, 10);
    NETHERITE_ARMOR = addMaterials("netherite", armor, LEATHER);
    TURTLE = addType(ItemType.TURTLE_HELMET, IRON_ARMOR);
    ARMADILLO = addType(ItemType.WOLF_ARMOR, DIAMOND_ARMOR);

    // See net.minecraft.world.item.ToolMaterial
    String[] tools = new String[] { "_axe", "_shovel", "_pickaxe", "_hoe", "_sword", "_spear" };
    WOOD = addMaterials("wooden", tools, LEATHER);
    STONE = addMaterials("stone", tools, 5);
    COPPER_TOOL = addMaterials("copper", tools, 13);
    IRON_TOOL = addMaterials("iron", tools, 14);
    GOLD_TOOL = addMaterials("golden", tools, 22);
    DIAMOND_TOOL = addMaterials("diamond", tools, DIAMOND_ARMOR);
    NETHERITE_TOOL = addMaterials("netherite", tools, NETHERITE_ARMOR);

    // See net.minecraft.world.item.Items
    BOOK = addType(ItemType.BOOK, new Enchantability(1));
    BY_KEY.put(ItemType.BOW.getKey(), BOOK);
    BY_KEY.put(ItemType.CROSSBOW.getKey(), BOOK);
    BY_KEY.put(ItemType.ENCHANTED_BOOK.getKey(), BOOK);
    BY_KEY.put(ItemType.FISHING_ROD.getKey(), BOOK);
    TRIDENT = addType(ItemType.TRIDENT, BOOK);
    MACE = addType(ItemType.MACE, LEATHER);
  }

  @Contract("_, _ -> param2")
  private static @NotNull Enchantability addType(
      @NotNull ItemType type,
      @NotNull Enchantability enchantability) {
    BY_KEY.put(type.getKey(), enchantability);
    return enchantability;
  }

  @Contract("_, _, _ -> new")
  private static @NotNull Enchantability addMaterials(
      @NotNull String materialName,
      @NotNull String @NotNull [] gearType,
      int value) {
    return addMaterials(materialName, gearType, new Enchantability(value));
  }

  @Contract("_, _, _ -> param3")
  private static @NotNull Enchantability addMaterials(
      @NotNull String materialName,
      @NotNull String @NotNull [] gearType,
      @NotNull Enchantability value) {
    for (String toolType : gearType) {
      BY_KEY.put(NamespacedKey.minecraft(materialName + toolType), value);
    }
    return value;
  }

}
