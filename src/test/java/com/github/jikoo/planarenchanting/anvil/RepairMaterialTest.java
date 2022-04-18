package com.github.jikoo.planarenchanting.anvil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import be.seeseemelk.mockbukkit.MockBukkit;
import com.github.jikoo.planarenchanting.util.mock.MockHelper;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@TestInstance(Lifecycle.PER_CLASS)
class RepairMaterialTest {

  @BeforeAll
  void beforeAll() {
    MockBukkit.mock();
  }

  @AfterAll
  void afterAll() {
    MockHelper.unmock();
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
          CROSSBOW -> false;
      default -> material.getMaxDurability() > 0;
    };
  }

}
