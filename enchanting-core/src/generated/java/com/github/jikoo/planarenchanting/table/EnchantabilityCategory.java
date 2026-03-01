package com.github.jikoo.planarenchanting.table;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.processing.Generated;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A provider for vanilla {@link Enchantability} categories.
 *
 * <p>All fields are generated from Minecraft internals and may be added or removed with
 * no notice. Please account for this.</p>
 *
 * <p>This file was generated from Minecraft 1.21.11. Regenerate it rather than modify.</p>
 */
@Generated("com.github.jikoo.planarenchanting.generator.impl.EnchantabilitiesGenerator")
@NullMarked
public final class EnchantabilityCategory {

  private static final Map<String, Enchantability> BY_NAME = new HashMap<>();

  @ApiStatus.Experimental
  public static final Enchantability WOOD_TOOL = add("WOOD_TOOL", 15);
  @ApiStatus.Experimental
  public static final Enchantability STONE_TOOL = add("STONE_TOOL", 5);
  @ApiStatus.Experimental
  public static final Enchantability COPPER_TOOL = add("COPPER_TOOL", 13);
  @ApiStatus.Experimental
  public static final Enchantability IRON_TOOL = add("IRON_TOOL", 14);
  @ApiStatus.Experimental
  public static final Enchantability DIAMOND_TOOL = add("DIAMOND_TOOL", 10);
  @ApiStatus.Experimental
  public static final Enchantability GOLD_TOOL = add("GOLD_TOOL", 22);
  @ApiStatus.Experimental
  public static final Enchantability NETHERITE_TOOL = add("NETHERITE_TOOL", 15);
  @ApiStatus.Experimental
  public static final Enchantability LEATHER_ARMOR = add("LEATHER_ARMOR", 15);
  @ApiStatus.Experimental
  public static final Enchantability COPPER_ARMOR = add("COPPER_ARMOR", 8);
  @ApiStatus.Experimental
  public static final Enchantability CHAINMAIL_ARMOR = add("CHAINMAIL_ARMOR", 12);
  @ApiStatus.Experimental
  public static final Enchantability IRON_ARMOR = add("IRON_ARMOR", 9);
  @ApiStatus.Experimental
  public static final Enchantability GOLD_ARMOR = add("GOLD_ARMOR", 25);
  @ApiStatus.Experimental
  public static final Enchantability DIAMOND_ARMOR = add("DIAMOND_ARMOR", 10);
  @ApiStatus.Experimental
  public static final Enchantability TURTLE_SCUTE_ARMOR = add("TURTLE_SCUTE_ARMOR", 9);
  @ApiStatus.Experimental
  public static final Enchantability NETHERITE_ARMOR = add("NETHERITE_ARMOR", 15);
  @ApiStatus.Experimental
  public static final Enchantability ARMADILLO_SCUTE_ARMOR = add("ARMADILLO_SCUTE_ARMOR", 10);

  private EnchantabilityCategory() {
  }

  /**
   * Safely try to access an {@link Enchantability} category by name.
   *
   * @param category the name of the category
   * @return the category if a match was found or {@code null} if no such category exists
   */
  public static final @Nullable Enchantability get(String category) {
    return BY_NAME.get(category.toUpperCase(Locale.ROOT));
  }

  private static final Enchantability add(String name, int value) {
    Enchantability enchantability = new Enchantability(value);
    BY_NAME.put(name, enchantability);
    return enchantability;
  }

}
