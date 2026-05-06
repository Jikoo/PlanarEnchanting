package com.github.jikoo.planarenchanting.generator;

import com.github.jikoo.planarenchanting.generator.impl.EnchantDataGenerator;
import com.github.jikoo.planarenchanting.generator.impl.EnchantabilityCategoryGenerator;
import com.github.jikoo.planarenchanting.generator.impl.EnchantableGenerator;
import com.github.jikoo.planarenchanting.generator.impl.RepairMaterialsGenerator;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.Registry.PendingTags;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.Util;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.WorldDataConfiguration;

public class Main {

  public static void main(String[] args) throws IOException {
    // Initialize server.
    SharedConstants.tryDetectVersion();
    Bootstrap.bootStrap();

    // Worldgen layer resources are required for data components and enchantments.

    // Set up built-in packs.
    PackRepository packRepository = ServerPacksSource.createVanillaTrustedRepository();
    FeatureFlagSet flags = FeatureFlags.REGISTRY.allFlags();
    MinecraftServer.configurePackRepository(
        packRepository,
        new WorldDataConfiguration(
            new DataPackConfig(
                FeatureFlags.REGISTRY.toNames(flags).stream().map(Identifier::getPath).toList(),
                List.of()
            ),
            flags
        ),
        true,
        false
    );

    try (MultiPackResourceManager manager = new MultiPackResourceManager(PackType.SERVER_DATA, packRepository.openAllSelected())) {
      LayeredRegistryAccess<RegistryLayer> access = RegistryLayer.createRegistryAccess();

      // Load worldgen layer for data-driven tags like enchantments.
      List<PendingTags<?>> pendingTags = TagLoader.loadTagsForExistingRegistries(
          manager,
          access.getLayer(RegistryLayer.STATIC)
      );
      List<RegistryLookup<?>> worldGenLookups = TagLoader.buildUpdatedLookups(
          access.getAccessForLoading(RegistryLayer.WORLDGEN),
          pendingTags
      );
      access = access.replaceFrom(
          RegistryLayer.WORLDGEN,
          RegistryDataLoader.load(
              manager,
              worldGenLookups,
              RegistryDataLoader.WORLDGEN_REGISTRIES,
              Util.backgroundExecutor()
          ).join()
      );

      access.compositeAccess().freeze();

      // Finalize tags.
      pendingTags.forEach(Registry.PendingTags::apply);
      // Initialize data components.
      BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(access.compositeAccess())
          .forEach(DataComponentInitializers.PendingComponents::apply);
    }

    // Set up generators.
    List<ItemConsumingGenerator> itemGens = List.of(
        new EnchantableGenerator(),
        new RepairMaterialsGenerator()
    );
    List<Generator> gens = new ArrayList<>(itemGens);
    gens.add(new EnchantabilityCategoryGenerator());
    gens.add(new EnchantDataGenerator());

    // Initialize TypeSpec being built and any other data.
    for (Generator gen : gens) {
      gen.buildSpec();
    }

    // Consume all items declared by the server.
    BuiltInRegistries.ITEM.entrySet().stream()
        // Sort by resource key so that ordering is consistent across generation runs.
        .sorted(Comparator.comparing(k -> k.getKey().identifier()))
        .forEach(entry -> {
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
