package com.github.jikoo.planarenchanting.anvil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MetaTempererTest {

  private MockedStatic<Bukkit> bukkit;

  @BeforeAll
  void setUp() {
    bukkit = mockStatic();

    bukkit.when(Bukkit::getItemFactory).thenAnswer(invocation -> {
      ItemFactory factory = mock();
      doReturn(false).when(factory).equals(any(), any());
      return factory;
    });
  }

  @AfterAll
  void tearDownAll() {
    bukkit.close();
  }

  @Test
  void hasChangedBaseNullMeta() {
    MetaCachedStack base = mock();

    RepairableMeta resultMeta = mock();
    MetaCachedStack result = mock();
    doReturn(resultMeta).when(result).getMeta();

    MetaCachedStack addition = mock();

    boolean hasChanged = assertDoesNotThrow(
        () -> MetaTemperer.INSTANCE.hasChanged(base, addition, result)
    );
    assertThat(hasChanged, is(false));
  }

  @Test
  void hasChangedResultNullMeta() {
    RepairableMeta baseMeta = mock();
    MetaCachedStack base = mock();
    doReturn(baseMeta).when(base).getMeta();

    MetaCachedStack result = mock();

    MetaCachedStack addition = mock();

    boolean hasChanged = assertDoesNotThrow(
        () -> MetaTemperer.INSTANCE.hasChanged(base, addition, result)
    );
    assertThat(hasChanged, is(false));
  }

  @Test
  void hasChangedBaseResultNullMeta() {
    MetaCachedStack base = mock();
    MetaCachedStack result = mock();
    MetaCachedStack addition = mock();

    boolean hasChanged = assertDoesNotThrow(
        () -> MetaTemperer.INSTANCE.hasChanged(base, addition, result)
    );
    assertThat(hasChanged, is(false));
  }

  @Test
  void hasChangedBaseNotRepairable() {
    ItemMeta baseMeta = mock();
    MetaCachedStack base = mock();
    doReturn(baseMeta).when(base).getMeta();

    RepairableMeta resultMeta = mock();
    doReturn(resultMeta).when(resultMeta).clone();
    MetaCachedStack result = mock();
    doReturn(resultMeta).when(result).getMeta();

    ItemStack additionStack = mock();
    MetaCachedStack addition = mock();
    doReturn(additionStack).when(addition).getItem();

    boolean hasChanged = assertDoesNotThrow(
        () -> MetaTemperer.INSTANCE.hasChanged(base, addition, result)
    );
    assertThat(hasChanged, is(true));
    verify(resultMeta, never()).setRepairCost(anyInt());
  }

  @Test
  void hasChangedResultNotRepairable() {
    RepairableMeta baseMeta = mock();
    MetaCachedStack base = mock();
    doReturn(baseMeta).when(base).getMeta();

    ItemMeta resultMeta = mock();
    doReturn(resultMeta).when(resultMeta).clone();
    MetaCachedStack result = mock();
    doReturn(resultMeta).when(result).getMeta();

    ItemStack additionStack = mock();
    MetaCachedStack addition = mock();
    doReturn(additionStack).when(addition).getItem();

    boolean hasChanged = assertDoesNotThrow(
        () -> MetaTemperer.INSTANCE.hasChanged(base, addition, result)
    );
    assertThat(hasChanged, is(true));
  }

  @Test
  void hasChangedBaseResultNotRepairable() {
    ItemMeta baseMeta = mock();
    MetaCachedStack base = mock();
    doReturn(baseMeta).when(base).getMeta();

    ItemMeta resultMeta = mock();
    doReturn(resultMeta).when(resultMeta).clone();
    MetaCachedStack result = mock();
    doReturn(resultMeta).when(result).getMeta();

    ItemStack additionStack = mock();
    MetaCachedStack addition = mock();
    doReturn(additionStack).when(addition).getItem();

    boolean hasChanged = assertDoesNotThrow(
        () -> MetaTemperer.INSTANCE.hasChanged(base, addition, result)
    );
    assertThat(hasChanged, is(true));
  }

  @Test
  void hasChangedAirAddition() {
    RepairableMeta baseMeta = mock();
    MetaCachedStack base = mock();
    doReturn(baseMeta).when(base).getMeta();

    RepairableMeta resultMeta = mock();
    doReturn(resultMeta).when(resultMeta).clone();
    MetaCachedStack result = mock();
    doReturn(resultMeta).when(result).getMeta();

    ItemStack additionStack = mock();
    doReturn(Material.AIR).when(additionStack).getType();
    MetaCachedStack addition = mock();
    doReturn(additionStack).when(addition).getItem();

    assertThat(
        "Differing result meta means a change",
        MetaTemperer.INSTANCE.hasChanged(base, addition, result),
        is(true)
    );

    verify(resultMeta, never()).setDisplayName(any());
  }

  @Test
  void hasChanged() {
    RepairableMeta baseMeta = mock();
    MetaCachedStack base = mock();
    doReturn(baseMeta).when(base).getMeta();

    RepairableMeta resultMeta = mock();
    doReturn(resultMeta).when(resultMeta).clone();
    MetaCachedStack result = mock();
    doReturn(resultMeta).when(result).getMeta();

    ItemStack additionStack = mock();
    doReturn(Material.DIRT).when(additionStack).getType();
    MetaCachedStack addition = mock();
    doReturn(additionStack).when(addition).getItem();

    assertThat(
        "Differing result meta means a change",
        MetaTemperer.INSTANCE.hasChanged(base, addition, result),
        is(true)
    );

    verify(resultMeta).setRepairCost(anyInt());
    verify(resultMeta).setDisplayName(any());
  }

  @Test
  void temper() {
    ItemStack stack = mock();
    MetaCachedStack metaStack = mock();
    doReturn(stack).when(metaStack).getItem();

    MetaTemperer.INSTANCE.temper(metaStack);

    verify(metaStack).getMeta();
    verify(stack).setItemMeta(any());
  }

  private interface RepairableMeta extends ItemMeta, Repairable {
    @Override
    @NonNull RepairableMeta clone();
  }

}