package com.github.jikoo.planarenchanting.anvil;

import org.jspecify.annotations.NullMarked;

/**
 * Interface defining {@link AnvilFunction AnvilFunctions} required to mimic vanilla behavior.
 *
 * @param <T> the type of the input and output items
 */
@NullMarked
public interface AnvilFunctionsProvider<T> {

  /**
   * Get an {@link AnvilFunction} applying the prior work cost to the result level cost.
   *
   * <p>Note that this does not apply the prior work cost to the result item!</p>
   *
   * @return the result level cost prior work application function
   */
  AnvilFunction<T> addPriorWorkLevelCost();

  /**
   * Get an {@link AnvilFunction} applying a rename operation. Applies new name and adds associated
   * cost.
   *
   * @return the name application function
   */
  AnvilFunction<T> rename();

  /**
   * Get an {@link AnvilFunction} applying the prior work cost to the result item.
   *
   * <p>Note that this does not apply the prior work cost to the result level cost!</p>
   *
   * @return the result item prior work application function
   */
  AnvilFunction<T> setItemPriorWork();

  /**
   * Get an {@link AnvilFunction} restoring durability to the base item using the secondary item.
   * Application incurs material costs.
   *
   * @return the function for repairing the base item
   */
  AnvilFunction<T> repairWithMaterial();

  /**
   * Get an {@link AnvilFunction} restoring durability to the base item by combining its durability
   * with that of a secondary item.
   *
   * @return the function for repairing the base item
   */
  AnvilFunction<T> repairWithCombine();

  /**
   * Get an {@link AnvilFunction} applying enchantments from the secondary item to the base item at
   * costs mimicking vanilla Java edition.
   *
   * @return the function for combining enchantments
   */
  AnvilFunction<T> combineEnchantsJava();

  /**
   * Get an {@link AnvilFunction} applying enchantments from the secondary item to the base item at
   * costs mimicking vanilla Bedrock edition. These are generally significantly lower than those of
   * Java edition.
   *
   * @return the function for combining enchantments
   */
  AnvilFunction<T> combineEnchantsBedrock();

}
