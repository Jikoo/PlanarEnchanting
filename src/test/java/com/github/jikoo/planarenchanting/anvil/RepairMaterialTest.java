package com.github.jikoo.planarenchanting.anvil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;

import com.github.jikoo.planarenchanting.util.mock.ServerMocks;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

@TestInstance(Lifecycle.PER_CLASS)
class RepairMaterialTest {

  @BeforeAll
  void beforeAll() {
    ServerMocks.mockServer();

    // RepairMaterial requires these tags.
    Tag<Material> tag = Tag.ITEMS_STONE_TOOL_MATERIALS;
    doReturn(Set.of(Material.STONE, Material.ANDESITE, Material.GRANITE, Material.DIORITE))
        .when(tag).getValues();
    tag = Tag.PLANKS;
    doReturn(Set.of(Material.ACACIA_PLANKS, Material.BIRCH_PLANKS, Material.OAK_PLANKS)) //etc. non-exhaustive list
        .when(tag).getValues();
  }

  @ParameterizedTest
  @EnumSource(value = Material.class, mode = Mode.MATCH_NONE, names = "LEGACY_.*")
  void test(@NotNull Material material) {
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
          BRUSH,
          WOLF_ARMOR-> false;
      default -> material.getMaxDurability() > 0;
    };
  }

}
