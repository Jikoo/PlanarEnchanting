package com.github.jikoo.planarenchanting.generator.util;

import com.palantir.javapoet.TypeName;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;

public final class FieldAccessor {

  /** A hack used to insert non-Javadoc comments in the field area. */
  public static final TypeName COMMENT;

  static {
    try {
      // ClassName checks name to ensure it is legal. Not good!
      Constructor<TypeName> constructor = TypeName.class.getDeclaredConstructor(String.class);
      constructor.setAccessible(true);
      COMMENT = constructor.newInstance("//");
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> void consumeFieldsOfType(Class<?> holder, Class<T> type, BiConsumer<String, T> consumer) {
    for (Field field : holder.getDeclaredFields()) {
      if (!type.isAssignableFrom(field.getType())
          || !Modifier.isStatic(field.getModifiers())) {
        continue;
      }
      try {
        consumer.accept(field.getName(), type.cast(field.get(null)));
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private FieldAccessor() {}

}
