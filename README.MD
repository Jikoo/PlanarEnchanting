# PlanarEnchanting

[![Build](https://github.com/Jikoo/PlanarEnchanting/actions/workflows/ci.yml/badge.svg)](https://github.com/Jikoo/PlanarEnchanting/actions/workflows/ci.yml)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Jikoo_PlanarEnchanting&metric=coverage)](https://sonarcloud.io/dashboard?id=Jikoo_PlanarEnchanting)  

## About

Enchanting is tied to a couple excessively complex systems in Minecraft.
Not only is there a lot of arcane math involved, it's all done in extremely long run-on methods that
are largely incomprehensible. Even with Mojang's mappings distributed, variable names inside methods
are not exposed, so you have to read the entirety of the code and understand exactly how it works
before attempting to manually deobfuscate it. PlanarEnchanting is an effort to take the enchanting
systems and divide them up into readable, manageable, customizable segments.

## Offerings

### Anvil Enchanting

Anvil enchanting is primarily accessed via the
[`AnvilCreator`](enchanting-bundler/src/main/java/com/github/jikoo/planarenchanting/anvil/AnvilCreator.java).
This is a wrapper allowing instantiation of implementation-specific anvil data.

For basic vanilla-style combination, all you need is an ordinary `Anvil`.

```java
class MyAnvilHandler implements Listener {
  private final Anvil anvil = AnvilCreator.create();
  @EventHandler
  private void onPrepareAnvil(PrepareAnvilEvent event) {
    AnvilView view = event.getView();
    AnvilResult result = anvil.getResult(view);

    event.setResult(result.item());

    // Note: Depending on the server implementation, you may need to
    // update costs on a 0-tick delay. It is also advisable to ensure
    // that the result item has not been changed when doing so.
    view.setRepairItemCostAmount(result.materialCost());
    view.setRepairCost(result.levelCost());
  }
}
```

For specific tweaks (i.e. removing or changing enchantment level cap) you can provide
a different `AnvilBehavior` implementation.

> [!IMPORTANT]
> For better compatibility, `Anvil` has two main implementations: component-based
> (using Paper's `DataComponentType`) and meta-based (using the Spigot API and `ItemMeta`).
> For detecting compatibility, PlanarEnchanting uses `ServerCapabilities.DATA_COMPONENT`.

Allowing conflicting enchantments to be added:

```java
// PlanarForge is the default Anvil implementation, wrapping a WorkPiece provider,
// an AnvilBehavior, and an AnvilFunctionsProvider.
Anvil anvil = new PlanarForge<ItemStack>(
    AnvilCreator::createComponentPiece,
    new ComponentVanillaBehavior() {
      @Override
      public boolean getEnchantsConflict(Enchantment enchant1, Enchantment enchant2) {
        return false;
      }
    },
    ComponentAnvilFunctions.INSTANCE
);
```

If you have even more specific needs but still want to leverage certain vanilla-style functionality,
you can write your own Anvil implementation. You can also implement your own
[AnvilFunctions](enchanting-common/src/main/java/com/github/jikoo/planarenchanting/anvil/AnvilFunction.java)
if you need additional behavior changes.

An implementation that only allows the input to be renamed:

```java
@NullMarked
class RenameOnlyAnvil<T> implements Anvil {

  protected final Function<AnvilView, WorkPiece<T>> createPiece;
  protected final AnvilBehavior<T> behavior;
  protected final AnvilFunctionsProvider<T> functions;

  RenameOnlyAnvil(
      Function<AnvilView, WorkPiece<T>> createPiece,
      AnvilBehavior<T> behavior,
      AnvilFunctionsProvider<T> functions
  ) {
    this.createPiece = createPiece;
    this.behavior = behavior;
    this.functions = functions;
  }
  
  @Override
  public AnvilResult getResult(AnvilView view) {
    WorkPiece<T> piece = createPiece.apply(view);

    // Require first item to be set, second item to be unset.
    if (piece.getBase().isEmpty() || !piece.getAddition().isEmpty()) {
      return AnvilResult.EMPTY;
    }

    // Apply base cost.
    piece.apply(behavior, functions.setPriorWorkCost());
    // Apply rename.
    piece.apply(behavior, functions.rename());

    return piece.forge();
  }
}
```

### Enchanting Table Enchanting

Enchanting Table-style enchanting is accessed via the [`EnchantingTable`](enchanting-core/src/main/java/com/github/jikoo/planarenchanting/table/EnchantingTable.java).

The constructor accepts a collection of enchantments that may be applied and the `Enchantability` (a
wrapper for a magic value representing an enchantment bonus; constants for vanilla values are
provided) of the item.

Minecraft's enchantment system is a mess of magic values, RNG, and a tiny bit of math. As there's no
really great way to expose that math and make it modifiable (you may as well just write your own
system then anyway), the enchantment system is not particularly customizable beyond what you see on
the surface. You may manipulate incompatibility between enchantments and enchantments' max levels.

Allow enchanting `Material.STONE` with enchantments usually available for tools:

```java
@NullMarked
class StoneEnchantListener extends TableEnchantListener {
  // Set up table.
  private final EnchantingTable table = new EnchantingTable(
      List.of(
          Enchantment.DIG_SPEED,
          Enchantment.DURABILITY,
          Enchantment.LOOT_BONUS_BLOCKS,
          Enchantment.SILK_TOUCH
      ),
      Enchantabilies.forMaterial(Material.STONE_PICKAXE)
  );

  StoneEnchantListener(Plugin plugin) {
    super(plugin);
  }

  @Override
  protected EnchantingTable getTable(Player player, ItemStack enchanted) {
    // Use stored instance.
    return table;
  }

  @Override
  protected boolean isIneligible(Player player, ItemStack enchanted) {
    // Only allow enchanting stone.
    return itemStack.getType() != Material.STONE;
  }
}
```

If you want more specific functionality you can write your own listener from scratch.

## Version Control

PlanarEnchanting is available via [JitPack](https://jitpack.io/#jikoo/planarenchanting).
JitPack supports Gradle, Maven, SBT, and Leiningen.

## For Developers

The project is compiled using Gradle via the `build` task, i.e. `./gradlew build`.  
Generated source files can be recreated from Minecraft internals with the `generate` task.
The generator module is not built by default so as to not require the Paper server to be compiled.
