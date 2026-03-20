package com.github.jikoo.planarenchanting.generator.impl;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

import com.github.jikoo.planarenchanting.generator.ItemConsumingGenerator;
import com.palantir.javapoet.AnnotationSpec;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantable;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class EnchantableGenerator extends ItemConsumingGenerator {

  private MethodSpec.@UnknownNullability Builder getter;

  public EnchantableGenerator() {
    super("com.github.jikoo.planarenchanting.table", "Enchantable");
  }

  @Override
  protected TypeSpec.Builder create() {
    ParameterizedTypeName setType = ParameterizedTypeName.get(
        ClassName.get(Set.class),
        ClassName.get(NamespacedKey.class).annotated(AnnotationSpec.builder(Nullable.class).build())
    );
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
        .addJavadoc(
            "Pre-baked data used as a fallthrough for {@link $T}.\n\n",
            ClassName.get(generatedClass.packageName(), "Enchantable")
        )
        // package-private; only Enchantabilities needs access.
        .addModifiers(FINAL)
        .addMethod(MethodSpec.constructorBuilder().addModifiers(PRIVATE).build());
  }

  @Override
  public void processItem(Identifier key, Item item) {
    Enchantable enchantable = item.components().get(DataComponents.ENCHANTABLE);
    if (enchantable != null) {
      getter.addStatement(
          "map.computeIfAbsent($L, create).add($T.fromString($S))",
          enchantable.value(), NamespacedKey.class, key.toString()
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
