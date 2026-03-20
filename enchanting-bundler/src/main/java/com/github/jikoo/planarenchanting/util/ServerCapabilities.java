package com.github.jikoo.planarenchanting.util;

/**
 * A container for constants tracking server capabilities.
 */
public final class ServerCapabilities {

  /** Whether the server supports Paper's {@code DataComponent} API. */
  public static final boolean DATA_COMPONENT = ComponentCapability.get();

  private ServerCapabilities() {
    throw new IllegalStateException("Cannot instantiate static helper method container.");
  }

}
