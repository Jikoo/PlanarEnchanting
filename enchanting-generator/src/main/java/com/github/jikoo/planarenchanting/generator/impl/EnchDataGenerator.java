package com.github.jikoo.planarenchanting.generator.impl;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

import com.github.jikoo.planarenchanting.generator.ItemConsumingGenerator;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.kyori.adventure.key.Key;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantable;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class EnchDataGenerator extends ItemConsumingGenerator {

  private MethodSpec.@UnknownNullability Builder getter;

  public EnchDataGenerator() {
    super("com.github.jikoo.planarenchanting.table", "Enchantable");
  }

  @Override
  protected TypeSpec.Builder create() {
    ParameterizedTypeName setType = ParameterizedTypeName.get(Set.class, Key.class);
    ParameterizedTypeName returnType = ParameterizedTypeName.get(
        ClassName.get(Map.class),
        TypeName.INT.box(), // ClassName.BOXED_INT isn't visible :(
        setType
    );
    ParameterizedTypeName functionType = ParameterizedTypeName.get(
        ClassName.get(Function.class),
        TypeName.INT.box(),
        setType
    );
    getter = MethodSpec.methodBuilder("get")
        .addModifiers(STATIC)
        .returns(returnType)
        .addStatement("$T map = new $T<>()", returnType, HashMap.class)
        .addStatement(
            "$T create = ignored -> new $T<>()",
            functionType, HashSet.class
        )
        .addComment(
            "<editor-fold defaultstate=\"collapsed\" desc=\"Generated from $L\">",
            Items.class.getName()
        );

    return TypeSpec.classBuilder(generatedClass)
        .addJavadoc("Pre-baked enchantment data used as a fallthrough for Enchantability.\n\n")
        // package-private; only Enchantability needs access.
        .addModifiers(FINAL)
        .addMethod(MethodSpec.constructorBuilder().addModifiers(PRIVATE).build());
  }

  @Override
  public void processItem(Identifier key, Item item) {
    Enchantable enchantable = item.components().get(DataComponents.ENCHANTABLE);
    if (enchantable != null) {
      getter.addStatement(
          "map.computeIfAbsent($L, create).add($T.key($S, $S))",
          enchantable.value(),Key.class, key.getNamespace(), key.getPath()
      );
    }
  }

  @Override
  public void generate(Path dir) throws IOException {
    getter.addComment("</editor-fold>");
    getter.addStatement("return map");
    builder.addMethod(getter.build());

    super.generate(dir);
  }

}
