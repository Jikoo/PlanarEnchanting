package com.github.jikoo.planarenchanting.anvil;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * Definitions of materials used in anvil repair operations.
 */
public final class RepairMaterial {

  /**
   * Get whether an item is repairable by another item.
   *
   * <p>N.B. This is for pure material-based repair operations, not for combination operations.
   *
   * @param base the item to be repaired
   * @param addition the item used to repair
   * @return whether the addition material can repair the base material
   */
  public static boolean repairs(@NotNull ItemStack base, @NotNull ItemStack addition) {
    RecipeChoice recipeChoice = MATERIALS_TO_REPAIRABLE.get(base.getType());
    return recipeChoice != null && recipeChoice.test(addition);
  }

  private static final Map<Material, RecipeChoice> MATERIALS_TO_REPAIRABLE = new HashMap<>();

  static {
    // Note that for all choices, we want to use a MaterialChoice.
    // ExactChoice also does a full meta match.

    // See net.minecraft.world.item.equipment.ArmorMaterials
    String[] armor = new String[] { "_HELMET", "_CHESTPLATE", "_LEGGINGS", "_BOOTS" };
    addGear("LEATHER", armor, Tag.ITEMS_REPAIRS_LEATHER_ARMOR);
    addGear("COPPER", armor, Tag.ITEMS_REPAIRS_COPPER_ARMOR);
    addGear("CHAINMAIL", armor, Tag.ITEMS_REPAIRS_CHAIN_ARMOR);
    addGear("IRON", armor, Tag.ITEMS_REPAIRS_IRON_ARMOR);
    addGear("GOLDEN", armor, Tag.ITEMS_REPAIRS_GOLD_ARMOR);
    addGear("DIAMOND", armor, Tag.ITEMS_REPAIRS_DIAMOND_ARMOR);
    MATERIALS_TO_REPAIRABLE.put(Material.TURTLE_HELMET, new MaterialChoice(Tag.ITEMS_REPAIRS_TURTLE_HELMET));
    addGear("NETHERITE", armor, Tag.ITEMS_REPAIRS_NETHERITE_ARMOR);
    MATERIALS_TO_REPAIRABLE.put(Material.WOLF_ARMOR, new MaterialChoice(Tag.ITEMS_REPAIRS_WOLF_ARMOR));

    // See net.minecraft.world.item.ToolMaterial
    String[] tools = new String[] { "_AXE", "_SHOVEL", "_PICKAXE", "_HOE", "_SWORD", "_SPEAR" };
    addGear("STONE", tools, Tag.ITEMS_STONE_TOOL_MATERIALS);
    MaterialChoice woodToolMats = new MaterialChoice(Tag.ITEMS_WOODEN_TOOL_MATERIALS);
    addGear("WOODEN", tools, woodToolMats);
    MATERIALS_TO_REPAIRABLE.put(Material.SHIELD, woodToolMats);
    addGear("IRON", tools, Tag.ITEMS_IRON_TOOL_MATERIALS);
    addGear("GOLDEN", tools, Tag.ITEMS_GOLD_TOOL_MATERIALS);
    addGear("DIAMOND", tools, Tag.ITEMS_GOLD_TOOL_MATERIALS);
    addGear("NETHERITE", tools, Tag.ITEMS_NETHERITE_TOOL_MATERIALS);

    // Misc. repairable items
    MATERIALS_TO_REPAIRABLE.put(Material.ELYTRA, new MaterialChoice(Material.PHANTOM_MEMBRANE));
    MATERIALS_TO_REPAIRABLE.put(Material.MACE, new MaterialChoice(Material.BREEZE_ROD));
  }

  private static void addGear(String type, String[] gearType, RecipeChoice repairChoice) {
    for (String toolType : gearType) {
      Material material = Material.getMaterial(type + toolType);
      if (material != null) {
        MATERIALS_TO_REPAIRABLE.put(material, repairChoice);
      }
    }
  }

  private static void addGear(String type, String[] gearType, Tag<Material> repairTag) {
    addGear(type, gearType, new MaterialChoice(repairTag));
  }

  @VisibleForTesting
  static boolean hasEntry(@NotNull Material material) {
    return MATERIALS_TO_REPAIRABLE.containsKey(material);
  }

  private RepairMaterial() {}

}
