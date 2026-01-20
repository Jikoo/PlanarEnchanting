package com.github.jikoo.planarenchanting.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doReturn;

import com.github.jikoo.planarenchanting.util.ItemUtil;
import com.github.jikoo.planarenchanting.util.mock.ServerMocks;
import com.github.jikoo.planarenchanting.util.mock.enchantments.EnchantmentMocks;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.MethodSource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EnchantabilityTest {

  @BeforeAll
  void setUpAll() {
    ServerMocks.mockServer();
    EnchantmentMocks.init();
    setUpTags();
  }

  private static void setUpTags() {
    doReturn(Set.of(Material.BOW)).when(Tag.ITEMS_ENCHANTABLE_BOW).getValues();
    doReturn(Set.of(Material.CROSSBOW)).when(Tag.ITEMS_ENCHANTABLE_CROSSBOW).getValues();
    doReturn(Set.of(Material.MACE)).when(Tag.ITEMS_ENCHANTABLE_MACE).getValues();
    doReturn(Set.of(Material.TRIDENT)).when(Tag.ITEMS_ENCHANTABLE_TRIDENT).getValues();
    doReturn(Set.of(Material.FISHING_ROD)).when(Tag.ITEMS_ENCHANTABLE_FISHING).getValues();

    Set<Material> lunge = new HashSet<>();
    Set<Material> sweeping = new HashSet<>();

    Set<Material> head = new HashSet<>();
    Set<Material> chest = new HashSet<>();
    Set<Material> leg = new HashSet<>();
    Set<Material> foot = new HashSet<>();

    Set<Material> axe = new HashSet<>();
    Set<Material> pick = new HashSet<>();
    Set<Material> tool = new HashSet<>();

    // Assemble main lists based on item name.
    // This helps catch issues like new materials not being included (copper),
    // but it doesn't help with new categories (spears).
    for (Material value : Material.values()) {
      String valueName = value.name();
      if (valueName.startsWith("LEGACY_")) {
        continue;
      }
      // Weapons
      if (valueName.endsWith("_SPEAR")) {
        lunge.add(value);
        continue;
      } else if (valueName.endsWith("_SWORD")) {
        sweeping.add(value);
        continue;
      }
      // Armor
      if (valueName.endsWith("_HELMET")) {
        head.add(value);
        continue;
      } else if (valueName.endsWith("_CHESTPLATE")) {
        chest.add(value);
        continue;
      } else if (valueName.endsWith("_LEGGINGS")) {
        leg.add(value);
        continue;
      } else if (valueName.endsWith("_BOOTS")) {
        foot.add(value);
        continue;
      }

      // Tools
      if (valueName.endsWith("_AXE")) {
        axe.add(value);
        tool.add(value);
      } else if (valueName.endsWith("_SHOVEL") ||  valueName.endsWith("_HOE")) {
        tool.add(value);
      } else if (valueName.endsWith("_PICKAXE")) {
        pick.add(value);
        tool.add(value);
      }
    }

    doReturn(lunge).when(Tag.ITEMS_ENCHANTABLE_LUNGE).getValues();
    doReturn(sweeping).when(Tag.ITEMS_ENCHANTABLE_SWEEPING).getValues();
    Set<Material> melee = new HashSet<>();
    melee.addAll(lunge);
    melee.addAll(sweeping);
    doReturn(melee).when(Tag.ITEMS_ENCHANTABLE_MELEE_WEAPON).getValues();
    Set<Material> fireAspect = new HashSet<>();
    fireAspect.add(Material.MACE);
    fireAspect.addAll(melee);
    doReturn(fireAspect).when(Tag.ITEMS_ENCHANTABLE_FIRE_ASPECT).getValues();
    Set<Material> sharp = new HashSet<>();
    sharp.addAll(melee);
    sharp.addAll(axe);
    doReturn(sharp).when(Tag.ITEMS_ENCHANTABLE_SHARP_WEAPON).getValues();
    Set<Material> weapon = new HashSet<>();
    weapon.add(Material.MACE);
    weapon.addAll(sharp);
    doReturn(weapon).when(Tag.ITEMS_ENCHANTABLE_WEAPON).getValues();

    doReturn(head).when(Tag.ITEMS_ENCHANTABLE_HEAD_ARMOR).getValues();
    doReturn(chest).when(Tag.ITEMS_ENCHANTABLE_CHEST_ARMOR).getValues();
    doReturn(leg).when(Tag.ITEMS_ENCHANTABLE_LEG_ARMOR).getValues();
    doReturn(foot).when(Tag.ITEMS_ENCHANTABLE_FOOT_ARMOR).getValues();
    Set<Material> armor = new HashSet<>();
    armor.addAll(head);
    armor.addAll(chest);
    armor.addAll(leg);
    armor.addAll(foot);
    doReturn(armor).when(Tag.ITEMS_ENCHANTABLE_ARMOR).getValues();

    doReturn(tool).when(Tag.ITEMS_ENCHANTABLE_MINING).getValues();
    doReturn(pick).when(Tag.ITEMS_ENCHANTABLE_MINING_LOOT).getValues();

    Set<Material> durability = new HashSet<>();
    durability.addAll(melee);
    durability.addAll(armor);
    durability.addAll(tool);
    durability.addAll(Set.of(
        Material.BOW,
        Material.BRUSH,
        Material.CARROT_ON_A_STICK,
        Material.CROSSBOW,
        Material.ELYTRA,
        Material.FISHING_ROD,
        Material.FLINT_AND_STEEL,
        Material.MACE,
        Material.SHEARS,
        Material.SHIELD,
        Material.TRIDENT,
        Material.WARPED_FUNGUS_ON_A_STICK
    ));
    doReturn(durability).when(Tag.ITEMS_ENCHANTABLE_DURABILITY).getValues();
  }

  @ParameterizedTest
  @MethodSource("com.github.jikoo.planarenchanting.util.mock.enchantments.EnchantmentMocks#getEnchantingTableTags")
  void verifySetup(Tag<Material> tag) {
    // A test test. The rabbit hole goes deeper!
    // This will help catch missed tags.
    if (!ItemUtil.TAG_EMPTY.equals(tag)) {
      assertThat(tag.getKey() + " is not empty", tag.getValues(), is(not(empty())));
    }
  }

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

  private static boolean isTableEnchantable(@NotNull Material material) {
    return switch (material) {
      // Enchanted book is technically not enchantable but for simplicity it's included.
      case BOOK, ENCHANTED_BOOK -> true;
      // Wolf armor has no valid enchantments, but it has an enchantability.
      case WOLF_ARMOR -> true;
      // Valid targets for unbreaking via anvil only.
      case BRUSH, CARROT_ON_A_STICK, ELYTRA, FLINT_AND_STEEL, SHEARS, SHIELD, WARPED_FUNGUS_ON_A_STICK -> false;
      default -> EnchantmentMocks.getEnchantingTableTags().stream()
          .anyMatch(tag -> tag.isTagged(material));
    };
  }

}
