package com.github.jikoo.planarenchanting.anvil;

import org.jetbrains.annotations.NotNull;

/**
 * An interface representing a portion of the functionality of an anvil. By using several in
 * conjunction, it is possible to mimic vanilla behavior very closely.
 */
public interface AnvilFunction {

  /**
   * Check if the function is capable of generating a usable result. Note that this may be a quick
   * cursory check - the function may yield an empty result even if it initially reported itself
   * applicable. The only guarantee this method makes is that retrieving and using a result will not
   * cause an error if its return value is respected.
   *
   * @param behavior the definition of behaviors for the anvil
   * @param state the {@link AnvilState} the state of the {@code AnvilOperation} in use
   * @return whether the {@link AnvilFunction} can generate an {@link AnvilFunctionResult}
   */
  boolean canApply(@NotNull AnvilBehavior behavior, @NotNull AnvilState state);

  /**
   * Get an {@link AnvilFunctionResult} used to apply the changes from the function based on the
   * provided anvil operation state and settings.
   *
   * @param behavior the definition of behaviors for the anvil
   * @param state the {@link AnvilState} the state of the anvil in use
   * @return the resulting applicable changes
   */
  @NotNull AnvilFunctionResult getResult(
      @NotNull AnvilBehavior behavior,
      @NotNull AnvilState state);

}
