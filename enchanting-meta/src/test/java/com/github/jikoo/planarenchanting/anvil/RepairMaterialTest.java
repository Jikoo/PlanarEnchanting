package com.github.jikoo.planarenchanting.anvil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentMatcher;
import org.mockito.MockedStatic;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RepairMaterialTest {

  private MockedStatic<Bukkit> bukkit;

  @BeforeAll
  void setUpAll() {
    bukkit = mockStatic();
    // Set up for Material tag for all diamond stuff
    ArgumentMatcher<NamespacedKey> isDiamondTag = key ->
        key != null && key.getKey().contains("diamond");
    bukkit.when(() -> Bukkit.getTag(eq("items"), argThat(isDiamondTag), eq(Material.class)))
        .thenAnswer(invocation -> {
          Tag<Material> tag = mock();
          doAnswer(invIsTagged -> {
            Material argument = invIsTagged.getArgument(0);
            return argument.getKey().getKey().contains("diamond");
          }).when(tag).isTagged(any());
          return tag;
        });
    // Set up for fallthrough to ItemType tag for all gold stuff
    ArgumentMatcher<NamespacedKey> isGoldTag = key ->
        key != null && key.getKey().contains("gold");
    bukkit.when(() -> Bukkit.getTag(eq("items"), argThat(isGoldTag), eq(ItemType.class)))
        .thenAnswer(invocation -> {
          Tag<ItemType> tag = mock();
          doAnswer(invIsTagged -> {
            ItemType argument = invIsTagged.getArgument(0);
            return argument.getKey().getKey().contains("gold");
          }).when(tag).isTagged(any());
          return tag;
        });
    // All other tags will be nonexistent
    // Set up other registries (Registry.MATERIAL is backed by the enum and doesn't need mocking)
    bukkit.when(() -> Bukkit.getRegistry(any())).thenAnswer(invocation -> mock(Registry.class));
    // TODO tests for list-typed?
  }

  @AfterAll
  void tearDownAll() {
    bukkit.close();
  }

  @Test
  void repairs() {
    ItemStack base = mock();
    doReturn(Material.DIAMOND_PICKAXE).when(base).getType();
    ItemStack addition = mock();
    doReturn(Material.DIAMOND).when(addition).getType();

    assertThat("Item is repairable", RepairMaterial.repairs(base, addition), is(true));
  }

  @Test
  void repairsNotRepairMat() {
    ItemStack base = mock();
    doReturn(Material.DIAMOND_PICKAXE).when(base).getType();
    ItemStack addition = mock();
    doReturn(Material.DIRT).when(addition).getType();

    assertThat("Item is repairable", RepairMaterial.repairs(base, addition), is(false));
  }

  @Test
  void repairsItemTypeFallthrough() {
    ItemStack base = mock();
    doReturn(Material.GOLDEN_PICKAXE).when(base).getType();

    NamespacedKey key = mock();
    doReturn("gold").when(key).getKey();
    ItemType type = mock();
    doReturn(key).when(type).getKey();
    Material material = mock();
    doReturn(type).when(material).asItemType();
    ItemStack addition = mock();
    doReturn(material).when(addition).getType();

    assertThat("Item is repairable", RepairMaterial.repairs(base, addition), is(true));
  }

  @Test
  void repairsNotRepairable() {
    ItemStack base = mock();
    doReturn(Material.WOODEN_PICKAXE).when(base).getType();
    ItemStack addition = mock();
    doReturn(Material.DIRT).when(addition).getType();

    assertThat("Item is not repairable", RepairMaterial.repairs(base, addition), is(false));
  }

}
