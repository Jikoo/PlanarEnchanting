package com.github.jikoo.planarenchanting.table;

import java.util.EnumMap;
import java.util.Map;
import org.bukkit.Material;
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

  public static final Enchantability LEATHER;
  public static final Enchantability CHAIN;
  public static final Enchantability IRON_ARMOR;
  public static final Enchantability GOLD_ARMOR;
  public static final Enchantability DIAMOND;
  public static final Enchantability TURTLE;
  public static final Enchantability NETHERITE;
  public static final Enchantability WOOD;
  public static final Enchantability STONE;
  public static final Enchantability IRON_TOOL;
  public static final Enchantability GOLD_TOOL;
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
    return BY_MATERIAL.get(material);
  }

  private static final Map<Material, Enchantability> BY_MATERIAL = new EnumMap<>(Material.class);

  static {
    String[] armor = new String[] { "_HELMET", "_CHESTPLATE", "_LEGGINGS", "_BOOTS" };
    LEATHER = addMaterials("LEATHER", armor, 15);
    CHAIN = addMaterials("CHAINMAIL", armor, 12);
    IRON_ARMOR = addMaterials("IRON", armor, 9);
    TURTLE = addMaterial(Material.TURTLE_HELMET, IRON_ARMOR);
    GOLD_ARMOR = addMaterials("GOLDEN", armor, 25);

    String[] tools = new String[] { "_AXE", "_SHOVEL", "_PICKAXE", "_HOE", "_SWORD" };
    WOOD = addMaterials("WOODEN", tools, LEATHER);
    BY_MATERIAL.put(Material.SHIELD, WOOD);
    BY_MATERIAL.put(Material.BOW, WOOD);
    BY_MATERIAL.put(Material.FISHING_ROD, WOOD);
    BY_MATERIAL.put(Material.CROSSBOW, WOOD);
    STONE = addMaterials("STONE", tools, 5);
    IRON_TOOL = addMaterials("IRON", tools, 14);
    GOLD_TOOL = addMaterials("GOLDEN", tools, 22);

    String[] armortools = new String[armor.length + tools.length];
    System.arraycopy(armor, 0, armortools, 0, armor.length);
    System.arraycopy(tools, 0, armortools, armor.length, tools.length);
    DIAMOND = addMaterials("DIAMOND", armortools, 10);
    NETHERITE = addMaterials("NETHERITE", armortools, LEATHER);

    BOOK = addMaterial(Material.BOOK, new Enchantability(1));
    BY_MATERIAL.put(Material.ENCHANTED_BOOK, BOOK);
    TRIDENT = addMaterial(Material.TRIDENT, BOOK);
    MACE = addMaterial(Material.MACE, LEATHER);
  }

  @Contract("_, _ -> param2")
  private static @NotNull Enchantability addMaterial(
      @NotNull Material material,
      @NotNull Enchantability enchantability) {
    BY_MATERIAL.put(material, enchantability);
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
      Material material = Material.getMaterial(materialName + toolType);
      if (material != null) {
        BY_MATERIAL.put(material, value);
      }
    }
    return value;
  }

}
