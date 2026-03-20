package com.github.jikoo.planarenchanting.generator.impl;

import static javax.lang.model.element.Modifier.PRIVATE;

import com.github.jikoo.planarenchanting.generator.ItemConsumingGenerator;
import com.mojang.datafixers.util.Either;
import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeSpec;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Repairable;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class RepairMaterialsGenerator extends ItemConsumingGenerator {

  private MethodSpec.@UnknownNullability Builder tagGetter;
  private MethodSpec.@UnknownNullability Builder listGetter;

  public RepairMaterialsGenerator() {
    super("com.github.jikoo.planarenchanting.anvil", "Repairable");
  }

  @Override
  protected TypeSpec.Builder create() {
    AnnotationSpec nullable = AnnotationSpec.builder(Nullable.class).build();

    ParameterizedTypeName returnType = ParameterizedTypeName.get(
        ClassName.get(Map.class),
        ClassName.get(NamespacedKey.class).annotated(nullable),
        ClassName.get(NamespacedKey.class).annotated(nullable)
    );
    tagGetter = MethodSpec.methodBuilder("getTags")
        .addModifiers(Modifier.STATIC)
        .returns(returnType)
        .addStatement("$T map = new $T<>()", returnType, HashMap.class)
        .addComment(
            "<editor-fold defaultstate=\"collapsed\" desc=\"Generated from $L\">",
            Items.class.getName()
        );

    returnType = ParameterizedTypeName.get(
        ClassName.get(Map.class),
        ClassName.get(NamespacedKey.class).annotated(nullable),
        ParameterizedTypeName.get(
            ClassName.get(List.class),
            ClassName.get(NamespacedKey.class).annotated(nullable)
        )
    );
    listGetter = MethodSpec.methodBuilder("getLists")
        .addModifiers(Modifier.STATIC)
        .returns(returnType)
        .addStatement("$T map = new $T<>()", returnType, HashMap.class)
        .addComment(
            "<editor-fold defaultstate=\"collapsed\" desc=\"Generated from $L\">",
            Items.class.getName()
        );

    return TypeSpec.classBuilder(generatedClass)
        .addJavadoc(
            "Pre-baked data used for {@link $T}.\n\n",
            ClassName.get(generatedClass.packageName(), "RepairMaterial")
        )
        // package-private; only RepairMaterial needs access.
        .addModifiers(Modifier.FINAL)
        .addMethod(MethodSpec.constructorBuilder().addModifiers(PRIVATE).build());
  }

  @Override
  public void processItem(Identifier key, Item item) {
    Repairable repairable = item.components().get(DataComponents.REPAIRABLE);

    if (repairable == null) {
      return;
    }

    Either<TagKey<Item>, List<Holder<Item>>> either = repairable.items().unwrap();

    either.map(
        tagKey -> {
          Identifier id = tagKey.location();
          tagGetter.addStatement(
              "map.put($T.fromString($S), $T.fromString($S))",
              NamespacedKey.class, key.toString(), NamespacedKey.class, id.toString()
          );
          return null;
        },
        list -> {
          if (list.isEmpty()) {
            return null;
          }
          listGetter.addCode(
              "map.put($T.fromString($S), $T.of(",
              NamespacedKey.class, key.toString(), List.class
          );

          boolean first = true;
          for (Holder<Item> holder : list) {
            if (first) {
              first = false;
            } else {
              listGetter.addCode(", ");
            }

            Item value = holder.value();
            Identifier id = BuiltInRegistries.ITEM.getKey(value);
            listGetter.addCode("$T.fromString($S)", NamespacedKey.class, id.toString());
          }

          listGetter.addStatement("))");
          return null;
        }
    );

  }

  @Override
  public void generate(Path dir) throws IOException {
    tagGetter.addComment("</editor-fold>");
    tagGetter.addStatement("return map");
    builder.addMethod(tagGetter.build());
    listGetter.addComment("</editor-fold>");
    listGetter.addStatement("return map");
    builder.addMethod(listGetter.build());

    super.generate(dir);
  }

}
