package com.github.jikoo.planarenchanting.generator.impl;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.STATIC;

import com.github.jikoo.planarenchanting.generator.Generator;
import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantment.Cost;
import net.minecraft.world.item.enchantment.Enchantments;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class EnchantDataGenerator extends Generator {

  private final ClassName enchantData;

  public EnchantDataGenerator() {
    // I know, it breaks the naming pattern, but BakedEnchantDataData felt a little too on the nose.
    super("com.github.jikoo.planarenchanting.util", "BakedEnchantData");
    enchantData = ClassName.get(generatedClass.packageName(), "EnchantData");
  }

  @Override
  protected TypeSpec.Builder create() {
    builder = TypeSpec.classBuilder(generatedClass).addModifiers(FINAL)
        .addJavadoc(
            "Pre-baked data used for {@link $T}.\n\n",
            ClassName.get(generatedClass.packageName(), "MetaEnchantProvider")
        );
    addGet(builder);
    addCreate(builder);
    return builder;
  }

  private void addGet(TypeSpec.Builder builder) {
    TypeName mapType = ParameterizedTypeName.get(
        ClassName.get(Map.class),
        ClassName.get(NamespacedKey.class).annotated(AnnotationSpec.builder(Nullable.class).build()),
        ParameterizedTypeName.get(
            ClassName.get(Function.class),
            ClassName.get(org.bukkit.enchantments.Enchantment.class),
            enchantData
        )
    );

    MethodSpec.Builder getter = MethodSpec.methodBuilder("get")
        .addModifiers(STATIC)
        .returns(mapType)
        .addStatement("$T map = new $T<>()", mapType, HashMap.class);

    getter.addComment(
            "<editor-fold defaultstate=\"collapsed\" desc=\"Generated from $L\">",
            Enchantments.class.getName()
        );

    // Produces similar lines to the following for each enchant:
    // load(NamespacedKey.fromString("minecraft:silk_touch"), create(1, 8, lvl -> 15, lvl -> 65));
    VanillaRegistries.createLookup()
        .lookupOrThrow(Registries.ENCHANTMENT)
        .listElements()
        .forEach(ref -> addEnchant(getter, ref));

    getter.addComment("</editor-fold>");
    getter.addStatement("return map");

    builder.addMethod(getter.build());
  }

  private static void addEnchant(MethodSpec.Builder getter, Reference<Enchantment> ref) {
    Identifier identifier = ref.key().identifier();
    getter.addCode("map.put($T.fromString($S), ", NamespacedKey.class, identifier.toString());

    Enchantment.EnchantmentDefinition def = ref.value().definition();
    getter.addCode("create($L, $L, ", def.weight(), def.anvilCost());
    addCost(getter, def.minCost());
    getter.addCode(", ");
    addCost(getter, def.maxCost());

    getter.addStatement("))");
  }

  private static void addCost(MethodSpec.Builder method, Cost cost) {
    if (cost.perLevelAboveFirst() == 0) {
      method.addCode("lvl -> $L", cost.base());
    } else {
      method.addCode("lvl -> $L + $L * (lvl - 1)", cost.base(), cost.perLevelAboveFirst());
    }
  }

  private void addCreate(TypeSpec.Builder builder) {
    builder.addMethod(
        MethodSpec.methodBuilder("create")
            .addModifiers(STATIC)
            .addParameter(int.class, "weight")
            .addParameter(int.class, "anvilCost")
            .addParameter(IntUnaryOperator.class, "minModCost")
            .addParameter(IntUnaryOperator.class, "maxModCost")
            .returns(
                ParameterizedTypeName.get(
                    ClassName.get(Function.class),
                    ClassName.get(org.bukkit.enchantments.Enchantment.class),
                    enchantData
                )
            )
            .addCode(
                """
                return enchant -> new $T() {
                  @Override
                  public int getWeight() {
                    return weight;
                  }

                  @Override
                  public int getAnvilCost() {
                    return anvilCost;
                  }

                  @Override
                  public int getMinModifiedCost(int level) {
                    return minModCost.applyAsInt(level);
                  }

                  @Override
                  public int getMaxModifiedCost(int level) {
                    return maxModCost.applyAsInt(level);
                  }

                  @Override
                  public boolean isTridentEnchant() {
                    return enchant.canEnchantItem(new $T($T.TRIDENT));
                  }
                };
                """,
                enchantData,
                ItemStack.class, Material.class
            )
            .build()
    );
  }

}
