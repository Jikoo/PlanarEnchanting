package com.github.jikoo.planarenchanting.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;

@DisplayName("Cache value from supplier")
@TestInstance(Lifecycle.PER_METHOD)
class CachedValueTest {

  @ParameterizedTest
  @MethodSource("getBooleans")
  void testCachedValue(Boolean value) {
    SingleUseSupplier<Boolean> singleUseSupplier = new SingleUseSupplier<>(value);
    var cache = new CachedValue<>(singleUseSupplier);

    assertThat("Value must be supplied as expected", cache.get(), is(value));
    assertThrows(
        AssertionFailedError.class,
        singleUseSupplier::get,
        "Supplier may only be used once");
    assertDoesNotThrow(cache::get, "Supplier must only be used once");
  }

  @Contract(pure = true)
  private static @NotNull Collection<Boolean> getBooleans() {
    return Arrays.asList(
        Boolean.TRUE,
        Boolean.FALSE,
        null
    );
  }

  private static class SingleUseSupplier<T> implements Supplier<T> {
    private final T value;
    private boolean used = false;

    private SingleUseSupplier(T value) {
      this.value = value;
    }

    @Override
    public T get() {
      if (used) {
        fail("Supplier may only be used once!");
      }
      used = true;
      return value;
    }

  }

}