package com.github.jikoo.planarenchanting.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentType.NonValued;
import io.papermc.paper.datacomponent.DataComponentType.Valued;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Enchantable;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;

@TestInstance(Lifecycle.PER_CLASS)
class ComponentEnchantabilitiesTest {

  private MockedStatic<RegistryAccess> registryAccess;
  private EnchantabilityProvider provider;

  @BeforeAll
  void setUp() {
    Answer<?> get = invocation -> switch (invocation.getArgument(0, Key.class).value()) {
      // Bit of a kludge, but there aren't many non-valued types.
      // Only alternative would be to reflectively examine fields here.
      case "unbreakable", "intangible_projectile", "glider" -> mock(NonValued.class);
      default -> mock(Valued.class);
    };

    registryAccess = mockStatic();
    registryAccess.when(RegistryAccess::registryAccess).thenAnswer(invRegAcc -> {
      RegistryAccess access = mock();
      // Deprecated method is required for legacy registry types like ART,
      // which will produce a NPE if not present when initializing Registry.
      doAnswer(invocation -> mock(Registry.class))
          .when(access).getRegistry(any(Class.class));
      doAnswer(invocation -> mock(Registry.class))
          .when(access).getRegistry(any(RegistryKey.class));
      doAnswer(invocation -> {
        Registry<DataComponentType> registry = mock();
        doAnswer(get).when(registry).getOrThrow(any(Key.class));
        return registry;
      }).when(access).getRegistry(RegistryKey.DATA_COMPONENT_TYPE);

      return access;
    });
    // touch types
    DataComponentTypes.REPAIR_COST.key();

    provider = new ComponentEnchantabilities();
  }

  @AfterAll
  void tearDown() {
    registryAccess.close();
  }

  @Test
  void ofMaterialNullItem() {
    Material material = mock();

    assertThat(
        "Non-item material does not have enchantability",
        provider.of(material),
        is(nullValue())
    );
  }

  @Test
  void ofMaterial() {
    Enchantable enchantable = mock();
    ItemType itemType = mock();
    doReturn(enchantable).when(itemType).getDefaultData(DataComponentTypes.ENCHANTABLE);
    Material material = mock();
    doReturn(itemType).when(material).asItemType();

    assertThat(
        "Material has enchantability",
        provider.of(material),
        is(notNullValue())
    );
  }

  @Test
  void ofItemTypeNotEnchantable() {
    ItemType itemType = mock();

    assertThat(
        "ItemType does not have enchantability",
        provider.of(itemType),
        is(nullValue())
    );
  }

  @Test
  void ofItemType() {
    Enchantable enchantable = mock();
    ItemType itemType = mock();
    doReturn(enchantable).when(itemType).getDefaultData(DataComponentTypes.ENCHANTABLE);

    assertThat(
        "ItemType has enchantability",
        provider.of(itemType),
        is(notNullValue())
    );
  }

  @Test
  void ofItemStackNotEnchantable() {
    ItemStack itemStack = mock();

    assertThat(
        "ItemStack does not have enchantability",
        provider.of(itemStack),
        is(nullValue())
    );
  }

  @Test
  void ofItemStack() {
    Enchantable enchantable = mock();
    ItemStack itemStack = mock();
    doReturn(enchantable).when(itemStack).getData(DataComponentTypes.ENCHANTABLE);

    assertThat(
        "ItemStack has enchantability",
        provider.of(itemStack),
        is(notNullValue())
    );
  }

}
