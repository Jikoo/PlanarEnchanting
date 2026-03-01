package com.github.jikoo.planarenchanting.util;

import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A simple container that pulls and caches a value from a {@link Supplier} on request.
 *
 * @param <T> the type of value to be cached
 */
public class CachedValue<T> {

  private @Nullable Supplier<T> supplier;
  private T value;

  /**
   * Construct a new {@code CachedValue}.
   *
   * @param supplier the {@link Supplier} for the cached value
   */
  public CachedValue(@NotNull Supplier<T> supplier) {
    this.supplier = supplier;
  }

  /**
   * Get the cached value. If the value is not cached, it will be fetched.
   *
   * @return the cached value
   */
  public T get() {
    if (supplier != null) {
      value = supplier.get();
      supplier = null;
    }

    return value;
  }

}
