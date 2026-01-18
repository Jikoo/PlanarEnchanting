package com.github.jikoo.planarenchanting.util.mock.server;

import io.papermc.paper.registry.RegistryKey;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import org.bukkit.Keyed;

@SuppressWarnings("unchecked")
enum RegistryHelper {
  ;

  private static final Map<Class<? extends Keyed>, RegistryKey<? extends Keyed>> LEGACY_REGISTRIES = new HashMap<>();

  static {
    forEachRegistryClass(((field, clazz) -> {
      try {
        LEGACY_REGISTRIES.put(clazz, (RegistryKey<? extends Keyed>) field.get(null));
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }));
  }

  static void forEachRegistryClass(BiConsumer<Field, Class<? extends Keyed>> consumer) {
    for (Field field : RegistryKey.class.getFields()) {
      if (field.getType() == RegistryKey.class) {
        Class<? extends Keyed> clazz = (Class<? extends Keyed>) getClass(((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
        consumer.accept(field, clazz);
      }
    }
  }

  private static Class<?> getClass(Type type) {
    if (type instanceof Class<?> clazz) {
      return clazz;
    } else if (type instanceof ParameterizedType parameterized) {
      return (Class<?>) parameterized.getRawType();
    } else {
      throw new UnsupportedOperationException("Unsupported type " + type.getClass());
    }
  }

  static <T extends Keyed> RegistryKey<T> getRegistryKey(Class<T> clazz) {
    return (RegistryKey<T>) LEGACY_REGISTRIES.get(clazz);
  }

}
