package com.github.jikoo.planarenchanting.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Cache value from supplier")
@TestInstance(Lifecycle.PER_METHOD)
class CachedValueTest {

  @ParameterizedTest
  @MethodSource("getBooleans")
  void testCachedValue(Boolean value) {
    Supplier<Boolean> supplier = mock();
    doReturn(value).when(supplier).get();
    var cache = new CachedValue<>(supplier);

    assertThat("Value must be supplied as expected", cache.get(), is(value));
    assertThat("Value must be supplied as expected", cache.get(), is(value));
    verify(supplier, only()).get();
  }

  private static Collection<Boolean> getBooleans() {
    return Arrays.asList(
        Boolean.TRUE,
        Boolean.FALSE,
        null
    );
  }

}