package com.github.jikoo.planarenchanting.anvil;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

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
    Predicate<NamespacedKey> predicate = MATERIALS_TO_REPAIRABLE.get(base.getType().getKey());
    return predicate != null && predicate.test(addition.getType().getKey());
  }

  private static final Map<NamespacedKey, Predicate<NamespacedKey>> MATERIALS_TO_REPAIRABLE = new HashMap<>();

  static {
    // Note that for all choices, we want to use a MaterialChoice.
    // ExactChoice also does a full meta match.

    // See net.minecraft.world.item.equipment.ArmorMaterials
    String[] armor = new String[] { "_helmet", "_chestplate", "_leggings", "_boots" };
    addGear("leather", armor, ItemTypeTagKeys.REPAIRS_LEATHER_ARMOR);
    addGear("copper", armor, ItemTypeTagKeys.REPAIRS_COPPER_ARMOR);
    addGear("chainmail", armor, ItemTypeTagKeys.REPAIRS_CHAIN_ARMOR);
    addGear("iron", armor, ItemTypeTagKeys.REPAIRS_IRON_ARMOR);
    addGear("golden", armor, ItemTypeTagKeys.REPAIRS_GOLD_ARMOR);
    addGear("diamond", armor, ItemTypeTagKeys.REPAIRS_DIAMOND_ARMOR);
    MATERIALS_TO_REPAIRABLE.put(ItemType.TURTLE_HELMET.getKey(), new TagPredicate(ItemTypeTagKeys.REPAIRS_TURTLE_HELMET));
    addGear("netherite", armor, ItemTypeTagKeys.REPAIRS_NETHERITE_ARMOR);
    MATERIALS_TO_REPAIRABLE.put(ItemType.WOLF_ARMOR.getKey(), new TagPredicate(ItemTypeTagKeys.REPAIRS_WOLF_ARMOR));

    // See net.minecraft.world.item.ToolMaterial
    String[] tools = new String[] { "_axe", "_shovel", "_pickaxe", "_hoe", "_sword", "_spear" };
    addGear("stone", tools, ItemTypeTagKeys.STONE_TOOL_MATERIALS);
    Predicate<NamespacedKey> woodToolMats = new TagPredicate(ItemTypeTagKeys.WOODEN_TOOL_MATERIALS);
    addGear("wooden", tools, woodToolMats);
    MATERIALS_TO_REPAIRABLE.put(ItemType.SHIELD.getKey(), woodToolMats);
    addGear("iron", tools, ItemTypeTagKeys.IRON_TOOL_MATERIALS);
    addGear("golden", tools, ItemTypeTagKeys.GOLD_TOOL_MATERIALS);
    addGear("diamond", tools, ItemTypeTagKeys.DIAMOND_TOOL_MATERIALS);
    addGear("netherite", tools, ItemTypeTagKeys.NETHERITE_TOOL_MATERIALS);

    // Misc. repairable items
    MATERIALS_TO_REPAIRABLE.put(ItemType.ELYTRA.getKey(), key -> ItemType.PHANTOM_MEMBRANE.getKey().equals(key));
    MATERIALS_TO_REPAIRABLE.put(ItemType.MACE.getKey(), key -> ItemType.BREEZE_ROD.getKey().equals(key));
  }

  private static void addGear(String type, String[] gearType, Predicate<NamespacedKey> repairChoice) {
    for (String toolType : gearType) {
      MATERIALS_TO_REPAIRABLE.put(NamespacedKey.minecraft(type + toolType), repairChoice);
    }
  }

  private static void addGear(String type, String[] gearType, TagKey<ItemType> tag) {
    addGear(type, gearType, new TagPredicate(tag));
  }

  private RepairMaterial() {
    throw new IllegalStateException("Cannot instantiate static helper method container.");
  }

  private record TagPredicate(Tag<ItemType> tag) implements Predicate<NamespacedKey> {

    private TagPredicate(TagKey<ItemType> tag) {
      this(RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM).getTag(tag));
    }

    @Override
    public boolean test(NamespacedKey key) {
      return tag.contains(TypedKey.create(RegistryKey.ITEM, key));
    }

  }

}
