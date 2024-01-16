package com.github.jikoo.planarenchanting.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Arrays;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentTarget;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

@TestInstance(Lifecycle.PER_METHOD)
class EnchantabilityTest {

  @ParameterizedTest
  @EnumSource(value = Material.class, mode = Mode.MATCH_NONE, names = "LEGACY_.*")
  void testForMaterial(@NotNull Material material) {
    boolean enchantable = isTableEnchantable(material);
    var value = Enchantability.forMaterial(material);
    if (enchantable) {
      assertThat("Enchantable material is listed", value, is(notNullValue()));
    } else {
      assertThat("Unenchantable material is not listed", value, is(nullValue()));
    }
  }

  private static boolean isTableEnchantable(Material material) {
    return switch (material) {
      // Enchanted book is technically not enchantable but for simplicity it's included.
      case BOOK, ENCHANTED_BOOK -> true;
      case CARROT_ON_A_STICK, WARPED_FUNGUS_ON_A_STICK, ELYTRA, FLINT_AND_STEEL, SHEARS, BRUSH ->
          false;
      default -> Arrays.stream(EnchantmentTarget.values())
          // Vanishable/wearable include items that cannot be enchanted except via anvils.
          // ALL no longer exists. The modern equivalent would likely be VANISHABLE.
          .filter(target -> !Set.of("VANISHABLE", "WEARABLE", "ALL").contains(target.name()))
          .anyMatch(target -> target.includes(material));
    };
  }

}
