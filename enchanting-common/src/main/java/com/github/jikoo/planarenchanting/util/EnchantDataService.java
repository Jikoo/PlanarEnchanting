package com.github.jikoo.planarenchanting.util;

import java.util.ServiceLoader;

import org.jetbrains.annotations.ApiStatus;

import com.github.jikoo.planarenchanting.util.EnchantData.Provider;

@ApiStatus.NonExtendable
public class EnchantDataService {

  /**
   * A {@link Provider} loaded from a service.
   */
  public static final Provider PROVIDER = ServiceLoader.load(Provider.class, Provider.class.getClassLoader())
      .findFirst().orElseThrow();

  private EnchantDataService() {
    throw new IllegalStateException("Cannot instantiate static helper container.");
  }

}