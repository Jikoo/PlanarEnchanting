package com.github.jikoo.planarenchanting.table;

import java.lang.reflect.Field;
import java.util.function.Supplier;
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
public final class Enchantabilities {

  @ApiStatus.Experimental
  public static final Enchantability WOOD_TOOL = new Enchantability(15);
  @ApiStatus.Experimental
  public static final Enchantability STONE_TOOL = new Enchantability(5);
  @ApiStatus.Experimental
  public static final Enchantability COPPER_TOOL = new Enchantability(13);
  @ApiStatus.Experimental
  public static final Enchantability IRON_TOOL = new Enchantability(14);
  @ApiStatus.Experimental
  public static final Enchantability DIAMOND_TOOL = new Enchantability(10);
  @ApiStatus.Experimental
  public static final Enchantability GOLD_TOOL = new Enchantability(22);
  @ApiStatus.Experimental
  public static final Enchantability NETHERITE_TOOL = new Enchantability(15);
  @ApiStatus.Experimental
  public static final Enchantability LEATHER_ARMOR = new Enchantability(15);
  @ApiStatus.Experimental
  public static final Enchantability COPPER_ARMOR = new Enchantability(8);
  @ApiStatus.Experimental
  public static final Enchantability CHAINMAIL_ARMOR = new Enchantability(12);
  @ApiStatus.Experimental
  public static final Enchantability IRON_ARMOR = new Enchantability(9);
  @ApiStatus.Experimental
  public static final Enchantability GOLD_ARMOR = new Enchantability(25);
  @ApiStatus.Experimental
  public static final Enchantability DIAMOND_ARMOR = new Enchantability(10);
  @ApiStatus.Experimental
  public static final Enchantability TURTLE_SCUTE_ARMOR = new Enchantability(9);
  @ApiStatus.Experimental
  public static final Enchantability NETHERITE_ARMOR = new Enchantability(15);
  @ApiStatus.Experimental
  public static final Enchantability ARMADILLO_SCUTE_ARMOR = new Enchantability(10);

  private Enchantabilities() {
  }

  /**
   * Safely try to access an {@link Enchantability} category by name.
   *
   * @param category the name of the category field
   * @return the category if a match was found or {@code null} if no such category exists
   */
  public static final @Nullable Enchantability get(String category) {
    try {
      Field field = Enchantabilities.class.getDeclaredField(category);
      return (Enchantability) field.get(null);
    } catch (ReflectiveOperationException | ClassCastException ignored) {
      return null;
    }
  }

  /**
   * Safely try to access an {@link Enchantability} category by name, falling through to a default
   * if not present.
   *
   * @param category the name of the category field
   * @param defaultSupplier the {@link Supplier} providing a fallthrough value
   * @return the category if a match was found or the default if no such category exists
   */
  public static final Enchantability getOrDefault(String category,
      Supplier<Enchantability> defaultSupplier) {
    Enchantability result = get(category);
    return result != null ? result : defaultSupplier.get();
  }

}
