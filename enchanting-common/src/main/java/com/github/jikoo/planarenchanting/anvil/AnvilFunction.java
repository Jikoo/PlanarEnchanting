package com.github.jikoo.planarenchanting.anvil;

import org.jspecify.annotations.NullMarked;

/**
 * An interface representing a portion of the functionality of an anvil. By using several in
 * conjunction, it is possible to mimic vanilla behavior very closely.
 *
 * @param <T> the type of the input and output items
 */
@NullMarked
public interface AnvilFunction<T> {

  /**
   * Check if the function is capable of generating a usable result. Note that this may be a quick
   * cursory check - the function may yield an empty result even if it initially reported itself
   * applicable. The only guarantee this method makes is that retrieving and using a result will not
   * cause an error if its return value is respected.
   *
   * @param behavior the definition of behaviors for the anvil
   * @param state the {@link ViewState} of the anvil in use
   * @param result the existing result
   * @return whether the {@link AnvilFunction} can generate an {@link AnvilFunctionResult}
   */
  boolean canApply(AnvilBehavior<T> behavior, ViewState<T> state, T result);

  /**
   * Get an {@link AnvilFunctionResult} used to apply the changes from the function based on the
   * provided anvil work piece and settings.
   *
   * @param behavior the definition of behaviors for the anvil
   * @param state the {@link ViewState} of the anvil in use
   * @param result the existing result
   * @return the resulting applicable changes
   */
  AnvilFunctionResult<T> getResult(AnvilBehavior<T> behavior, ViewState<T> state, T result);

}
