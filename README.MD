# PlanarEnchanting

![Build](https://github.com/Jikoo/PlanarEnchanting/workflows/Build/badge.svg)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Jikoo_PlanarEnchanting&metric=coverage)](https://sonarcloud.io/dashboard?id=Jikoo_PlanarEnchanting)  
Yet another Bukkit-related library.

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
[`AnvilOperation`](src/main/java/com/github/jikoo/planarenchanting/anvil/AnvilOperation.java).
This is a class containing several simple ways to modify anvil behaviors.

For basic vanilla-style combination, all you need is an ordinary `AnvilOperation`.

```java
class MyAnvilHandler implements org.bukkit.event.Listener {
  @org.bukkit.event.EventHandler
  private void onPrepareAnvil(org.bukkit.event.inventory.PrepareAnvilEvent event) {
    org.bukkit.inventory.AnvilInventory inventory = event.getInventory();
    com.github.jikoo.planarenchanting.anvil.AnvilOperation operation = new AnvilOperation();
    com.github.jikoo.planarenchanting.anvil.AnvilResult result = operation.apply(inventory);

    event.setResult(result.item());
    inventory.setRepairCostAmount(result.materialCost());

    // Note: depending on how anvil actually functions, may need to update cost on a 0-tick delay.
    inventory.setRepairCost(result.levelCost());
  }
}
```

For specific use cases (i.e. removing or changing enchantment level cap) you can provide the
`AnvilOperation` with different functions which will be used by the default `AnvilFunction`
implementations.

Allowing conflicting enchantments to be added:
```java
AnvilOperation operation = new AnvilOperation();
operation.setEnchantsConflict((enchant1, enchant2) -> false);
```

If you have even more specific needs but still want to leverage certain vanilla-style functionality,
you can write your own `AnvilOperation` and override `AnvilOperation#apply` to set your own
operation order.

A custom `AnvilOperation` that only performs rename operations:

```java
public class RenameOperation extends AnvilOperation {
  @Override
  public void apply(@NotNull AnvilInventory inventory) {
    AnvilOperationState state = new AnvilOperationState(this, inventory);

    if (ItemUtil.isEmpty(state.getBase().getItem())) {
      return AnvilResult.EMPTY;
    }

    // Apply base cost.
    state.apply(AnvilFunction.PRIOR_WORK_LEVEL_COST);
    // Apply rename.
    state.apply(AnvilFunction.RENAME);

    return state.forge();
  }
}
```


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

Replace `$planarVersion` with the version you desire to work with. The `minimizeJar` option is
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
      <version>$planarVersion</version>
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
              <minimizeJar>true</minimizeJar>
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
