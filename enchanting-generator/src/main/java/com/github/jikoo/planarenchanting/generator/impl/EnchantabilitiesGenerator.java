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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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
    super("com.github.jikoo.planarenchanting.table", "EnchantabilityCategory");
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
    addByName();

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
    category = category.toUpperCase(Locale.ROOT);
    FieldSpec.Builder fieldBuilder = FieldSpec.builder(enchantability, category)
        .addModifiers(PUBLIC, STATIC, FINAL)
        .addAnnotation(ApiStatus.Experimental.class)
        .initializer("add($S, $L)", category, value);
    builder.addField(fieldBuilder.build());
  }

  private void addByName() {
    builder.addField(
        FieldSpec.builder(
            ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                enchantability
            ),
            "BY_NAME",
            PRIVATE, STATIC, FINAL
        ).initializer("new $T<>()", HashMap.class).build());

    MethodSpec get = MethodSpec.methodBuilder("get")
        .addJavadoc(
            """
            Safely try to access an {@link $T} category by name.
            
            @param category the name of the category
            @return the category if a match was found or {@code null} if no such category exists
            """,
            enchantability
        )
        .addModifiers(PUBLIC, STATIC, FINAL)
        .returns(enchantability.annotated(AnnotationSpec.builder(Nullable.class).build()))
        .addParameter(String.class, "category")
        .addStatement("return BY_NAME.get(category.toUpperCase($T.ROOT))", Locale.class)
        .build();
    builder.addMethod(get);

    MethodSpec add = MethodSpec.methodBuilder("add")
        .addModifiers(PRIVATE, STATIC, FINAL)
        .returns(enchantability)
        .addParameter(String.class, "name")
        .addParameter(int.class, "value")
        .addStatement("$T enchantability = new $T(value)", enchantability, enchantability)
        .addStatement("BY_NAME.put(name, enchantability)")
        .addStatement("return enchantability")
        .build();
    builder.addMethod(add);
  }

}
