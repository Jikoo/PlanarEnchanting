package com.github.jikoo.planarenchanting.util.mock.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.github.jikoo.planarenchanting.util.mock.inventory.ItemStackMocks;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.mockito.stubbing.Answer;

public class TestRegistryAccess implements RegistryAccess {

  private static final Map<RegistryKey<? extends Keyed>, Registry<? extends @NotNull Keyed>> REGISTERS = new HashMap<>();

  @Override
  @Deprecated(since = "1.20.6", forRemoval = true)
  public @Nullable <T extends Keyed> Registry<@NotNull T> getRegistry(@NotNull Class<T> type) {
    return getRegistry(RegistryHelper.getRegistryKey(type));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends @NotNull Keyed> @NotNull Registry<@NotNull T> getRegistry(@NotNull RegistryKey<T> registryKey) {
    return (Registry<T>) REGISTERS.computeIfAbsent(registryKey, regKey -> {
      Registry<?> registry = mock();

      AtomicReference<Class<? extends Keyed>> clazzRef = new AtomicReference<>();
      RegistryHelper.forEachRegistryClass((field, clazz) -> {
        if (RegistryHelper.getRegistryKey(clazz).equals(regKey)) {
          clazzRef.set(clazz);
        }
      });
      Map<NamespacedKey, Keyed> cache = new HashMap<>();
      Class<? extends Keyed> clazz = clazzRef.get();

      Answer<Keyed> getOrThrow = invocationGetEntry -> {
        NamespacedKey key = invocationGetEntry.getArgument(0);
        // Some classes (like BlockType and ItemType) have extra generics that will be
        // erased during runtime calls. To ensure accurate typing, grab the constant's field.
        // This approach also allows us to return null for unsupported keys.
        Class<? extends Keyed> constantClazz;
        try {
          constantClazz = (Class<? extends Keyed>) clazz.getField(key.getKey().toUpperCase(
              Locale.ROOT).replace('.', '_')).getType();
        } catch (ClassCastException | NoSuchFieldException e) {
          throw new RuntimeException(e);
        }

        return cache.computeIfAbsent(key, key1 -> {
          Keyed keyed = mock(constantClazz);
          doReturn(key).when(keyed).getKey();
          if (keyed instanceof ItemType itemType) {
            setUp(itemType);
          }
          return keyed;
        });
      };

      doAnswer(getOrThrow).when(registry).getOrThrow(any(NamespacedKey.class));
      // For get, return null for nonexistant constants.
      doAnswer(invocation -> {
        try {
          return getOrThrow.answer(invocation);
        } catch (RuntimeException e) {
          if (e.getCause() instanceof NoSuchFieldException) {
            return null;
          }
          throw e;
        }
      }).when(registry).get(any(NamespacedKey.class));

      return registry;
    });
  }

  private static void setUp(@NotNull ItemType itemType) {
    // ItemStack creation.
    doAnswer(
        invocation -> {
          itemType.createItemStack(1);
          return null;
        }
    ).when(itemType).createItemStack();
    doAnswer(invocation -> ItemStackMocks.newItemMock(itemType, invocation.getArgument(0)))
        .when(itemType).createItemStack(anyInt());

    // Lazy ItemType -> Material mapping for use in ItemStack creation.
    Map<ItemType, Material> asMaterial = new ConcurrentHashMap<>();
    doAnswer(invocation -> asMaterial.computeIfAbsent(itemType, type -> {
      for (Material material : Material.values()) {
        if (!material.isLegacy() && material.getKey().equals(itemType.getKey())) {
          return material;
        }
      }
      throw new IllegalStateException("Unable to locate Material for ItemType " + itemType.getKey());
    })).when(itemType).asMaterial();
  }

}
