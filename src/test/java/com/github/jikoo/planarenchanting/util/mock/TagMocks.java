package com.github.jikoo.planarenchanting.util.mock;

import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Set;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;

public final class TagMocks {

  public static <T extends Keyed> void mockTag(
      @NotNull Server server,
      @NotNull String registry,
      @NotNull NamespacedKey key,
      @NotNull Class<T> clazz,
      Collection<T> values) {

    // Server should already be a mock
    when(server.getTag(registry, key, clazz)).thenAnswer(invocation -> {
      Tag<?> mock = mock(Tag.class);

      when(mock.getKey()).thenReturn(key);
      when(mock.isTagged(notNull())).thenAnswer(invocation1 -> {
        T argument = invocation1.getArgument(0);
        return values.contains(argument);
      });
      when(mock.getValues()).thenAnswer(invocation1 -> Set.copyOf(values));

      return mock;
    });
  }

  private TagMocks() {}

}
