package com.github.jikoo.planarenchanting.generator.impl;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

import com.github.jikoo.planarenchanting.generator.Generator;
import com.github.jikoo.planarenchanting.generator.util.FieldAccessor;
import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeSpec;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class EnchantabilitiesGenerator extends Generator {

  private final ClassName enchantability;

  public EnchantabilitiesGenerator() {
    super("com.github.jikoo.planarenchanting.table", "Enchantabilities");
    // Can't reference actual type because that would introduce a circular dependency
    // unless we add a really convoluted partial compile dependency or generate the record here.
    this.enchantability = ClassName.get(generatedClass.packageName(), "Enchantability");
  }

  @Override
  protected TypeSpec.Builder create() {
    builder = TypeSpec.classBuilder(generatedClass)
        .addModifiers(PUBLIC, FINAL)
        .addJavadoc(
            """
            A provider for vanilla {@link $T} categories.
            
            <p>All fields are generated from Minecraft internals and may be added or removed with
            no notice. Please account for this.</p>
            
            """,
            enchantability
        )
        .addMethod(MethodSpec.constructorBuilder().addModifiers(PRIVATE).build());

    addCategories();
    addAccessors();

    return builder;
  }

  private void addCategories() {
    FieldAccessor.consumeFieldsOfType(ToolMaterial.class, ToolMaterial.class, (name, mat) -> {
      String category = name.toUpperCase(Locale.ROOT) + "_TOOL";
      addCategory(category, mat.enchantmentValue());
    });

    FieldAccessor.consumeFieldsOfType(ArmorMaterials.class, ArmorMaterial.class, (name, mat) -> {
      String category = name.toUpperCase(Locale.ROOT) + "_ARMOR";
      addCategory(category, mat.enchantmentValue());
    });
  }

  private void addCategory(String category, int value) {
    FieldSpec.Builder fieldBuilder = FieldSpec.builder(enchantability, category.toUpperCase(Locale.ROOT))
        .addModifiers(PUBLIC, STATIC, FINAL)
        .addAnnotation(ApiStatus.Experimental.class)
        .initializer("new $T($L)", enchantability, value);
    builder.addField(fieldBuilder.build());
  }

  private void addAccessors() {
    MethodSpec get = MethodSpec.methodBuilder("get")
        .addJavadoc(
            """
            Safely try to access an {@link $T} category by name.
            
            @param category the name of the category field
            @return the category if a match was found or {@code null} if no such category exists
            """,
            enchantability
        )
        .addModifiers(PUBLIC, STATIC, FINAL)
        .returns(enchantability.annotated(AnnotationSpec.builder(Nullable.class).build()))
        .addParameter(String.class, "category")
        .addCode("""
            try {
              $T field = $T.class.getDeclaredField(category);
              return ($T) field.get(null);
            } catch ($T | $T ignored) {
              return null;
            }
            """,
            Field.class, generatedClass,
            enchantability,
            // ClassCastException shouldn't be possible as all fields are Enchantability typed, but
            // it's better to be safe than sorry.
            ReflectiveOperationException.class, ClassCastException.class
        )
        .build();

    MethodSpec getOrDefault = MethodSpec.methodBuilder("getOrDefault")
        .addJavadoc(
            """
            Safely try to access an {@link $T} category by name, falling through to a default
            if not present.
            
            @param category the name of the category field
            @param defaultSupplier the {@link $T} providing a fallthrough value
            @return the category if a match was found or the default if no such category exists
            """,
            enchantability,
            Supplier.class
        )
        .addModifiers(PUBLIC, STATIC, FINAL)
        .returns(enchantability)
        .addParameter(String.class, "category")
        .addParameter(
            ParameterizedTypeName.get(ClassName.get(Supplier.class), enchantability),
            "defaultSupplier"
        )
        .addCode("""
            $T result = get(category);
            return result != null ? result : defaultSupplier.get();
            """,
            enchantability
        )
        .build();

    builder.addMethod(get).addMethod(getOrDefault);
  }

}
