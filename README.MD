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
[`Anvil`](src/main/java/com/github/jikoo/planarenchanting/anvil/Anvil.java).
This is a class containing several simple ways to modify anvil behaviors.

For basic vanilla-style combination, all you need is an ordinary `Anvil`.

```java
class MyAnvilHandler implements Listener {
  private final Anvil anvil = new Anvil();
  @EventHandler
  private void onPrepareAnvil(PrepareAnvilEvent event) {
    AnvilView view = event.getView();
    AnvilResult result = anvil.getResult(view);

    event.setResult(result.item());

    // Note: depending on server implementation, may need to update costs on a 0-tick delay.
    view.setRepairItemCostAmount(result.materialCost());
    view.setRepairCost(result.levelCost());
  }
}
```

For specific tweaks (i.e. removing or changing enchantment level cap) you can provide the
`Anvil` with a different `AnvilBehavior` implementation.

Allowing conflicting enchantments to be added:

```java
Anvil anvil = new Anvil(new AnvilBehavior() {
  @Override
  public boolean getEnchantsConflict(Enchantment enchant1, Enchantment enchant2) {
    return false;
  }
});
```

If you have even more specific needs but still want to leverage certain vanilla-style functionality,
you can write your own anvil functionality.

An implementation that only allows the input to be renamed:

```java
class RenameOnlyAnvil extends Anvil {
  @Override
  public void getResult(@NotNull AnvilView view) {
    AnvilState state = new AnvilState(view);

    // Require first item to be set, second item to be unset.
    if (ItemUtil.isEmpty(state.getBase().getItem())
        || !ItemUtil.isEmpty(state.getAddition().getItem())) {
      return AnvilResult.EMPTY;
    }

    // Apply base cost.
    apply(state, AnvilFunctions.PRIOR_WORK_LEVEL_COST);
    // Apply rename.
    apply(state, AnvilFunctions.RENAME);

    return forge(state);
  }
}
```

### Enchanting Table Enchanting

Enchanting Table-style enchanting is accessed via the [`EnchantingTable`](src/main/java/com/github/jikoo/planarenchanting/table/EnchantingTable.java).

The constructor accepts a collection of enchantments that may be applied and the `Enchantability` (a
wrapper for a magic value representing an enchantment bonus; constants for vanilla values are
provided) of the item.

Minecraft's enchantment system is a mess of magic values, RNG, and a tiny bit of math. As there's no
really great way to expose that math and make it modifiable (you may as well just write your own
system then anyway), the enchantment system is not particularly customizable beyond what you see on
the surface. You may manipulate incompatibility between enchantments and enchantments' max levels.

Allow enchanting `Material.STONE` with enchantments usually available for tools:

```java
class StoneEnchantListener extends TableEnchantListener {

  // Set up table.
  private final EnchantingTable table = new EnchantingTable(
      List.of(
          Enchantment.DIG_SPEED,
          Enchantment.DURABILITY,
          Enchantment.LOOT_BONUS_BLOCKS,
          Enchantment.SILK_TOUCH),
      Enchantability.STONE);

  StoneEnchantListener(@NotNull Plugin plugin) {
    super(plugin);
  }

  @Override
  protected @Nullable EnchantingTable getTable(
      @NotNull Player player,
      @NotNull ItemStack enchanted) {
    // Use stored instance.
    return table;
  }

  @Override
  protected boolean isIneligible(
      @NotNull Player player,
      @NotNull ItemStack enchanted) {
    // Only allow enchanting stone.
    return itemStack.getType() != Material.STONE;
  }

}
```

If you want more specific functionality you can write your own listener from scratch.

## For Developers

Please relocate PlanarEnchanting when including its files in your project! Bundled library conflicts
are not fun, make your life easier.

### License [![WTFPL](http://www.wtfpl.net/wp-content/uploads/2012/12/wtfpl-badge-2.png)](http://www.wtfpl.net/)

This project is licensed under the WTFPL. You can do whatever you want with the content.
If you do use it and decide to credit me, thanks! I think you're just swell.

### Version Control

PlanarEnchanting is available via [JitPack](https://jitpack.io). I pretty much only use Maven in my
projects, so that's the only full writeup you get, but JitPack supports Gradle, Maven, SBT, and
Leiningen.

### Maven

Replace `${versions.planarenchanting}` with the version you desire to work with or define it in your properties. The `minimizeJar` option is
recommended to prevent inflating your plugin with unnecessary classes.

Sample configuration:

```xml
<project>
  <repositories>
    <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
    </repository>
  </repositories>

  <dependencies>
    <dependency>
      <groupId>com.github.jikoo</groupId>
      <artifactId>planarenchanting</artifactId>
      <version>${versions.planarenchanting}</version>
      <scope>compile</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <version>3.3.0</version>
        <executions>
          <execution>
            <phase>package</phase>
            <goals>
              <goal>shade</goal>
            </goals>
            <configuration>
              <relocations>
                <relocation>
                  <pattern>com.github.jikoo.planarenchanting</pattern>
                  <shadedPattern>com.example.myplugin.planarenchanting</shadedPattern>
                </relocation>
              </relocations>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```
