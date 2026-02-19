package com.github.jikoo.planarenchanting.generator;

import com.github.jikoo.planarenchanting.generator.impl.EnchDataGenerator;
import com.github.jikoo.planarenchanting.generator.impl.EnchantabilitiesGenerator;
import com.github.jikoo.planarenchanting.generator.impl.RepairMaterialsGenerator;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;

public class Main {

  public static void main(String[] args) throws IOException {
    // Initialize server.
    SharedConstants.tryDetectVersion();
    Bootstrap.bootStrap();

    // Set up generators.
    List<ItemConsumingGenerator> itemGens = List.of(
        new EnchDataGenerator(),
        new RepairMaterialsGenerator()
    );
    List<Generator> gens = new ArrayList<>(itemGens);
    gens.add(new EnchantabilitiesGenerator());

    // Initialize TypeSpec being built and any other data.
    for (Generator gen : gens) {
      gen.buildSpec();
    }

    // Consume all items declared by the server.
    BuiltInRegistries.ITEM.entrySet().forEach(entry -> {
      for (ItemConsumingGenerator gen : itemGens) {
        gen.processItem(entry.getKey().identifier(), entry.getValue());
      }
    });

    // Output the files.
    Path destPath = Path.of(args.length > 0 ? args[0] : ".");
    for (Generator gen : gens) {
      gen.generate(destPath);
    }
  }
}
