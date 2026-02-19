package com.github.jikoo.planarenchanting.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.github.jikoo.planarenchanting.util.mock.ServerMocks;
import com.github.jikoo.planarenchanting.util.mock.enchantments.EnchantmentMocks;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Enchantable;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.ItemTypeKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EnchantabilityTest {

  @BeforeAll
  void setUpAll() throws IllegalAccessException {
    ServerMocks.mockServer();
    EnchantmentMocks.init();
    setUpTags();

    // Touch Enchantable type so we can use it as a parameter when mocking.
    var componentType = DataComponentTypes.ENCHANTABLE;

    // Mock all baked data.
    BakedEnchantableData.get().forEach((value, holders) -> {
      Enchantable enchantable = mock();
      doReturn(value).when(enchantable).value();
      for (Key key : holders) {
        ItemType itemType = Registry.ITEM.get(key);
        doReturn(enchantable).when(itemType).getDefaultData(componentType);
      }
    });
  }

  private static void setUpTags() throws IllegalAccessException {
    setValues(ItemTypeTagKeys.ENCHANTABLE_BOW, Set.of(ItemTypeKeys.BOW));
    setValues(ItemTypeTagKeys.ENCHANTABLE_CROSSBOW, Set.of(ItemTypeKeys.CROSSBOW));
    setValues(ItemTypeTagKeys.ENCHANTABLE_MACE, Set.of(ItemTypeKeys.MACE));
    setValues(ItemTypeTagKeys.ENCHANTABLE_TRIDENT, Set.of(ItemTypeKeys.TRIDENT));
    setValues(ItemTypeTagKeys.ENCHANTABLE_FISHING, Set.of(ItemTypeKeys.FISHING_ROD));

    Set<TypedKey<ItemType>> lunge = new HashSet<>();
    Set<TypedKey<ItemType>> sweeping = new HashSet<>();

    Set<TypedKey<ItemType>> head = new HashSet<>();
    Set<TypedKey<ItemType>> chest = new HashSet<>();
    Set<TypedKey<ItemType>> leg = new HashSet<>();
    Set<TypedKey<ItemType>> foot = new HashSet<>();

    Set<TypedKey<ItemType>> axe = new HashSet<>();
    Set<TypedKey<ItemType>> pick = new HashSet<>();
    Set<TypedKey<ItemType>> tool = new HashSet<>();

    // Assemble main lists based on item name.
    // This helps catch issues like new materials not being included (copper),
    // but it doesn't help with new categories (spears).
    for (Field field : ItemTypeKeys.class.getFields()) {
      if (field.getType() != TypedKey.class) {
        continue;
      }
      @SuppressWarnings("unchecked")
      TypedKey<ItemType> value = (TypedKey<ItemType>) field.get(null);
      String name = field.getName();
      // Weapons
      if (name.endsWith("_SPEAR")) {
        lunge.add(value);
        continue;
      } else if (name.endsWith("_SWORD")) {
        sweeping.add(value);
        continue;
      }
      // Armor
      if (name.endsWith("_HELMET")) {
        head.add(value);
        continue;
      } else if (name.endsWith("_CHESTPLATE")) {
        chest.add(value);
        continue;
      } else if (name.endsWith("_LEGGINGS")) {
        leg.add(value);
        continue;
      } else if (name.endsWith("_BOOTS")) {
        foot.add(value);
        continue;
      }

      // Tools
      if (name.endsWith("_AXE")) {
        axe.add(value);
        tool.add(value);
      } else if (name.endsWith("_SHOVEL") || name.endsWith("_HOE")) {
        tool.add(value);
      } else if (name.endsWith("_PICKAXE")) {
        pick.add(value);
        tool.add(value);
      }
    }

    setValues(ItemTypeTagKeys.ENCHANTABLE_LUNGE, lunge);
    setValues(ItemTypeTagKeys.ENCHANTABLE_SWEEPING, sweeping);
    Set<TypedKey<ItemType>> melee = new HashSet<>();
    melee.addAll(lunge);
    melee.addAll(sweeping);
    setValues(ItemTypeTagKeys.ENCHANTABLE_MELEE_WEAPON, melee);
    Set<TypedKey<ItemType>> fireAspect = new HashSet<>();
    fireAspect.add(ItemTypeKeys.MACE);
    fireAspect.addAll(melee);
    setValues(ItemTypeTagKeys.ENCHANTABLE_FIRE_ASPECT, fireAspect);
    Set<TypedKey<ItemType>> sharp = new HashSet<>();
    sharp.addAll(melee);
    sharp.addAll(axe);
    setValues(ItemTypeTagKeys.ENCHANTABLE_SHARP_WEAPON, sharp);
    Set<TypedKey<ItemType>> weapon = new HashSet<>();
    weapon.add(ItemTypeKeys.MACE);
    weapon.addAll(sharp);
    setValues(ItemTypeTagKeys.ENCHANTABLE_WEAPON, weapon);

    setValues(ItemTypeTagKeys.ENCHANTABLE_HEAD_ARMOR, head);
    setValues(ItemTypeTagKeys.ENCHANTABLE_CHEST_ARMOR, chest);
    setValues(ItemTypeTagKeys.ENCHANTABLE_LEG_ARMOR, leg);
    setValues(ItemTypeTagKeys.ENCHANTABLE_FOOT_ARMOR, foot);
    Set<TypedKey<ItemType>> armor = new HashSet<>();
    armor.addAll(head);
    armor.addAll(chest);
    armor.addAll(leg);
    armor.addAll(foot);
    setValues(ItemTypeTagKeys.ENCHANTABLE_ARMOR, armor);

    setValues(ItemTypeTagKeys.ENCHANTABLE_MINING, tool);
    setValues(ItemTypeTagKeys.ENCHANTABLE_MINING_LOOT, pick);

    Set<TypedKey<ItemType>> durability = new HashSet<>();
    durability.addAll(melee);
    durability.addAll(armor);
    durability.addAll(tool);
    durability.addAll(Set.of(
        ItemTypeKeys.BOW,
        ItemTypeKeys.BRUSH,
        ItemTypeKeys.CARROT_ON_A_STICK,
        ItemTypeKeys.CROSSBOW,
        ItemTypeKeys.ELYTRA,
        ItemTypeKeys.FISHING_ROD,
        ItemTypeKeys.FLINT_AND_STEEL,
        ItemTypeKeys.MACE,
        ItemTypeKeys.SHEARS,
        ItemTypeKeys.SHIELD,
        ItemTypeKeys.TRIDENT,
        ItemTypeKeys.WARPED_FUNGUS_ON_A_STICK
    ));
    setValues(ItemTypeTagKeys.ENCHANTABLE_DURABILITY, durability);
  }

  private static void setValues(TagKey<ItemType> key, Set<TypedKey<ItemType>> values) {
    // We need to pull the tag first so the test registry has finished mocking it.
    var tag = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM).getTag(key);
    // Then we can safely mock our values.
    doReturn(values).when(tag).values();
  }

  @ParameterizedTest
  @MethodSource("com.github.jikoo.planarenchanting.util.mock.enchantments.EnchantmentMocks#getEnchantingTableTags")
  void verifySetup(Tag<ItemType> tag) {
    // A test test. The rabbit hole goes deeper!
    // This will help catch missed tags.
    assertThat(tag.tagKey() + " is not empty", tag.values(), is(not(empty())));
  }

  @ParameterizedTest
  @MethodSource("getMaterials")
  void forMaterial(@NotNull Material material) {
    var value = Enchantability.forMaterial(material);
    if (material.isItem() && isTableEnchantable(material.asItemType())) {
      assertThat("Enchantable material is listed", value, is(notNullValue()));
    } else {
      assertThat("Unenchantable material is not listed", value, is(nullValue()));
    }
  }

  @ParameterizedTest
  @MethodSource("getTypes")
  void forType(@NotNull ItemType material) {
    var value = Enchantability.forType(material);
    if (isTableEnchantable(material)) {
      assertThat("Enchantable material is listed", value, is(notNullValue()));
    } else {
      assertThat("Unenchantable material is not listed", value, is(nullValue()));
    }
  }

  @Test
  void forItemEnchantable() {
    Enchantable enchantable = mock();
    doReturn(100).when(enchantable).value();
    var item = ItemType.DIAMOND.createItemStack();
    doReturn(enchantable).when(item).getData(DataComponentTypes.ENCHANTABLE);

    assertThat(
        "ItemStack enchantable component must be used",
        Enchantability.forItem(item),
        is(new Enchantability(enchantable.value()))
    );
  }

  @Test
  void forItemUnenchantable() {
    var item = ItemType.DIAMOND.createItemStack();
    doReturn(null).when(item).getData(DataComponentTypes.ENCHANTABLE);

    assertThat(
        "ItemStack without enchantable component has no enchantability",
        Enchantability.forItem(item),
        is(nullValue())
    );
  }

  static Stream<Material> getMaterials() {
    return Arrays.stream(Material.values())
        .filter(material -> !material.name().startsWith("LEGACY_"));
  }

  static Stream<ItemType> getTypes() {
    return getMaterials()
        .filter(Material::isItem)
        .map(Material::asItemType);
  }

  static boolean isTableEnchantable(@Nullable ItemType material) {
    if (material == null) {
      return false;
    }
    return switch (material.key().value()) {
      case "book" -> true;
      // Wolf armor "enchantability" is a placeholder because ArmorMaterial requires one.
      // It isn't actually used.
      case "wolf_armor" -> false;
      // Valid targets for unbreaking via anvil only.
      case "brush", "carrot_on_a_stick", "elytra", "flint_and_steel", "shears", "shield", "warped_fungus_on_a_stick" -> false;
      default -> {
        TypedKey<ItemType> typedKey = TypedKey.create(RegistryKey.ITEM, material.key());
        yield EnchantmentMocks.getEnchantingTableTags().stream()
          .anyMatch(tag -> tag.contains(typedKey));
      }
    };
  }

}
