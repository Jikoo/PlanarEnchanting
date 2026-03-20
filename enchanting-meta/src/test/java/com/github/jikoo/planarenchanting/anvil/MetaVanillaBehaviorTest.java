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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatcher;
import org.mockito.MockedStatic;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MetaVanillaBehaviorTest {

  private MockedStatic<Bukkit> bukkit;
  private MetaVanillaBehavior behavior;

  @BeforeAll
  void setUp() {
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
  }

  @AfterAll
  void tearDownAll() {
    bukkit.close();
  }

  @BeforeEach
  void setUpEach() {
    behavior = new MetaVanillaBehavior();
  }

  @ParameterizedTest
  @ValueSource(booleans = { true, false })
  void enchantApplies(boolean applies) {
    Enchantment enchantment = mock();
    doReturn(applies).when(enchantment).canEnchantItem(any());
    assertThat(
        "Behavior applies if enchant applies",
        behavior.enchantApplies(enchantment, mock()),
        is(applies)
    );
    verify(enchantment).canEnchantItem(any());
    verifyNoMoreInteractions(enchantment);
  }

  @ParameterizedTest
  @CsvSource({
      "DIAMOND,DIAMOND,true",
      "DIAMOND_PICKAXE,DIAMOND,false",
      "DIAMOND,ENCHANTED_BOOK,true",
      "ENCHANTED_BOOK,ENCHANTED_BOOK,true",
      "ENCHANTED_BOOK,DIAMOND,false"
  })
  void itemsCombineEnchants(Material baseMat, Material additionMat, boolean result) {
    ItemStack baseStack = mock();
    doReturn(baseMat).when(baseStack).getType();
    MetaCachedStack base = mock();
    doReturn(baseStack).when(base).getItem();
    ItemStack additionStack = mock();
    doReturn(additionMat).when(additionStack).getType();
    MetaCachedStack addition = mock();
    doReturn(additionStack).when(addition).getItem();

    assertThat(
        "Like items and enchanted book additions combine enchantments",
        behavior.itemsCombineEnchants(base, addition),
        is(result)
    );
  }

  @Test
  void itemRepairedBy() {
    ItemStack baseStack = mock();
    doReturn(Material.DIAMOND_PICKAXE).when(baseStack).getType();
    MetaCachedStack base = mock();
    doReturn(baseStack).when(base).getItem();
    ItemStack additionStack = mock();
    doReturn(Material.DIAMOND).when(additionStack).getType();
    MetaCachedStack addition = mock();
    doReturn(additionStack).when(addition).getItem();

    assertThat("Item is repairable", behavior.itemRepairedBy(base, addition), is(true));
  }

  @Test
  void repairsNotRepairMat() {
    ItemStack baseStack = mock();
    doReturn(Material.DIAMOND_PICKAXE).when(baseStack).getType();
    MetaCachedStack base = mock();
    doReturn(baseStack).when(base).getItem();
    ItemStack additionStack = mock();
    doReturn(Material.DIRT).when(additionStack).getType();
    MetaCachedStack addition = mock();
    doReturn(additionStack).when(addition).getItem();

    assertThat("Item is not repairable", behavior.itemRepairedBy(base, addition), is(false));
  }

  @Test
  void repairsItemTypeFallthrough() {
    ItemStack baseStack = mock();
    doReturn(Material.GOLDEN_PICKAXE).when(baseStack).getType();
    MetaCachedStack base = mock();
    doReturn(baseStack).when(base).getItem();

    NamespacedKey key = mock();
    doReturn("gold").when(key).getKey();
    ItemType type = mock();
    doReturn(key).when(type).getKey();
    Material material = mock();
    doReturn(type).when(material).asItemType();
    ItemStack additionStack = mock();
    doReturn(material).when(additionStack).getType();
    MetaCachedStack addition = mock();
    doReturn(additionStack).when(addition).getItem();

    assertThat("Item is repairable", behavior.itemRepairedBy(base, addition), is(true));
  }

  @Test
  void repairsNotRepairable() {
    ItemStack baseStack = mock();
    doReturn(Material.WOODEN_PICKAXE).when(baseStack).getType();
    MetaCachedStack base = mock();
    doReturn(baseStack).when(base).getItem();
    ItemStack additionStack = mock();
    doReturn(Material.DIRT).when(additionStack).getType();
    MetaCachedStack addition = mock();
    doReturn(baseStack).when(addition).getItem();

    assertThat("Item is not repairable", behavior.itemRepairedBy(base, addition), is(false));
  }

}
