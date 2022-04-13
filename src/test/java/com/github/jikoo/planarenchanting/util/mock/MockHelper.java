package com.github.jikoo.planarenchanting.util.mock;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.bukkit.plugin.Plugin;

public final class MockHelper {

  public static void unmock() {
    ServerMock server = MockBukkit.getMock();
    for (Plugin plugin : server.getPluginManager().getPlugins()) {
      server.getScheduler().cancelTasks(plugin);
    }
    MockBukkit.unmock();
  }

  private MockHelper() {
    throw new IllegalStateException("Cannot instantiate static helper method container.");
  }

}
