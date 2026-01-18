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
    return BY_MATERIAL.get(material);
  }

  private static final Map<Material, Enchantability> BY_MATERIAL = new EnumMap<>(Material.class);

  static {
    // See net.minecraft.world.item.equipment.ArmorMaterials
    String[] armor = new String[] { "_HELMET", "_CHESTPLATE", "_LEGGINGS", "_BOOTS" };
    LEATHER = addMaterials("LEATHER", armor, 15);
    COPPER_ARMOR = addMaterials("COPPER", armor, 8);
    CHAIN = addMaterials("CHAINMAIL", armor, 12);
    IRON_ARMOR = addMaterials("IRON", armor, 9);
    GOLD_ARMOR = addMaterials("GOLDEN", armor, 25);
    DIAMOND_ARMOR = addMaterials("DIAMOND", armor, 10);
    NETHERITE_ARMOR = addMaterials("NETHERITE", armor, LEATHER);
    TURTLE = addMaterial(Material.TURTLE_HELMET, IRON_ARMOR);
    ARMADILLO = addMaterial(Material.WOLF_ARMOR, DIAMOND_ARMOR);

    // See net.minecraft.world.item.ToolMaterial
    String[] tools = new String[] { "_AXE", "_SHOVEL", "_PICKAXE", "_HOE", "_SWORD", "_SPEAR" };
    WOOD = addMaterials("WOODEN", tools, LEATHER);
    STONE = addMaterials("STONE", tools, 5);
    COPPER_TOOL = addMaterials("COPPER", tools, 13);
    IRON_TOOL = addMaterials("IRON", tools, 14);
    GOLD_TOOL = addMaterials("GOLDEN", tools, 22);
    DIAMOND_TOOL = addMaterials("DIAMOND", tools, DIAMOND_ARMOR);
    NETHERITE_TOOL = addMaterials("NETHERITE", tools, NETHERITE_ARMOR);

    // See net.minecraft.world.item.Items
    BOOK = addMaterial(Material.BOOK, new Enchantability(1));
    BY_MATERIAL.put(Material.BOW, BOOK);
    BY_MATERIAL.put(Material.CROSSBOW, BOOK);
    BY_MATERIAL.put(Material.ENCHANTED_BOOK, BOOK);
    BY_MATERIAL.put(Material.FISHING_ROD, BOOK);
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
