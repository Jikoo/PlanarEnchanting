package com.github.jikoo.planarenchanting.anvil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.jikoo.planarenchanting.util.mock.ServerMocks;
import com.github.jikoo.planarenchanting.util.mock.TagMocks;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@TestInstance(Lifecycle.PER_CLASS)
class RepairMaterialTest {

  @BeforeAll
  void beforeAll() {
    Server server = ServerMocks.mockServer();

    // RepairMaterial requires these tags.
    TagMocks.mockTag(server, "items", NamespacedKey.minecraft("stone_tool_materials"), Material.class,
        List.of(Material.STONE, Material.ANDESITE, Material.GRANITE, Material.DIORITE));
    TagMocks.mockTag(server, "blocks", NamespacedKey.minecraft("planks"), Material.class,
        List.of(Material.ACACIA_PLANKS, Material.BIRCH_PLANKS, Material.OAK_PLANKS)); //etc. non-exhaustive list

    Bukkit.setServer(server);
  }

  @ParameterizedTest
  @EnumSource(Material.class)
  void test(@NotNull Material material) {
    if (material.isLegacy()) {
      return;
    }
    if (isRepairable(material)) {
      assertThat("Material with durability has entry", RepairMaterial.hasEntry(material), is(true));
    } else {
      assertThat("Material without durability is not listed", RepairMaterial.hasEntry(material), is(false));
    }
  }

  private static boolean isRepairable(@NotNull Material material) {
    return switch (material) {
      case CARROT_ON_A_STICK,
          WARPED_FUNGUS_ON_A_STICK,
          FLINT_AND_STEEL,
          BOW,
          FISHING_ROD,
          SHEARS,
          TRIDENT,
          CROSSBOW,
          BRUSH -> false;
      default -> material.getMaxDurability() > 0;
    };
  }

}
