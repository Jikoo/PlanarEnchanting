package com.github.jikoo.planarenchanting.anvil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.bukkit.Material;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

@NullMarked
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PlanarForgeTest {

  private AnvilView view;
  private AnvilInventory inventory;
  private ItemStack base;
  private ItemStack addition;
  private AnvilFunctionsProvider<Void> functions;
  private PlanarForge<Void> anvil;
  private AnvilResult forgeResult;

  @BeforeEach
  void beforeEach() {
    view = mock();
    inventory = mock();
    doReturn(inventory).when(view).getTopInventory();

    base = mock();
    doReturn(1).when(base).getAmount();
    doReturn(base).when(inventory).getItem(0);
    addition = mock();
    doReturn(addition).when(inventory).getItem(1);

    ViewState<Void> state = mock();

    AnvilBehavior<Void> behavior = mock();
    functions = mock(Mockito.RETURNS_MOCKS);
    forgeResult = mock();
    anvil = new PlanarForge<>(
        view -> {
          WorkPiece<Void> piece = mock();
          doReturn(forgeResult).when(piece).temper();
          doAnswer(invocation -> {
            AnvilFunction<Void> function = invocation.getArgument(1);
            if (function.canApply(behavior, state, mock())) {
              function.getResult(behavior, state, mock());
              return true;
            }
            return false;
          }).when(piece).apply(any(), any());
          return piece;
        },
        behavior,
        functions
    );
  }

  @Test
  void getResultNullBase() {
    doReturn(null).when(inventory).getItem(0);

    assertThat(
        "Result is empty for null base.",
        anvil.getResult(view).item().getType() == Material.AIR,
        is(true)
    );
  }

  @Test
  void getResultEmptyBase() {
    doReturn(Material.AIR).when(base).getType();

    assertThat(
        "Result is empty for empty base.",
        anvil.getResult(view).item().getType() == Material.AIR,
        is(true)
    );
  }

  @Test
  void getResultNullAddition() {
    doReturn(null).when(inventory).getItem(1);

    assertThat(
        "Result is empty for null addition and no rename.",
        anvil.getResult(view).item().getType() == Material.AIR,
        is(true)
    );

    verify(functions).addPriorWorkLevelCost();
    // No addition means only a rename.
    verify(functions).rename();
  }

  @Test
  void getResultEmptyAddition() {
    doReturn(Material.AIR).when(addition).getType();

    assertThat(
        "Result is empty for empty addition and no rename.",
        anvil.getResult(view).item().getType() == Material.AIR,
        is(true)
    );

    verify(functions).addPriorWorkLevelCost();
    verify(functions).rename();
  }

  @Test
  void getResultEmptyAdditionRename() {
    doReturn(Material.AIR).when(addition).getType();
    AnvilFunction<Void> rename = mock();
    doReturn(true).when(rename).canApply(any(), any(), any());
    AnvilFunctionResult<Void> result = mock();
    doReturn(result).when(rename).getResult(any(), any(), any());
    doReturn(rename).when(functions).rename();

    assertThat(
        "Result is forged for rename with no addition",
        anvil.getResult(view),
        is(sameInstance(forgeResult))
    );

    verify(functions).addPriorWorkLevelCost();
    verify(functions).rename();
    verify(functions, never()).setItemPriorWork();
  }

  @Test
  void getResultMultipleBase() {
    doReturn(2).when(base).getAmount();

    assertThat(
        "Result is empty for multiple base with addition.",
        anvil.getResult(view).item().getType() == Material.AIR,
        is(true)
    );

    verify(functions).addPriorWorkLevelCost();
    verify(functions, never()).rename();
  }

  @Test
  void getResult() {
    assertThat("Result is expected value", anvil.getResult(view), is(forgeResult));

    verify(functions).addPriorWorkLevelCost();
    verify(functions).rename();
    verify(functions).setItemPriorWork();
    verify(functions).repairWithMaterial();
    verify(functions).repairWithCombine();
    verify(functions).combineEnchantsJava();
  }

  @Test
  void getResultMaterialRepair() {
    AnvilFunction<Void> repairWithMaterial = mock();
    doReturn(true).when(repairWithMaterial).canApply(any(), any(), any());
    AnvilFunctionResult<Void> result = mock();
    doReturn(result).when(repairWithMaterial).getResult(any(), any(), any());
    doReturn(repairWithMaterial).when(functions).repairWithMaterial();

    assertThat("Result is expected value", anvil.getResult(view), is(forgeResult));

    verify(functions).addPriorWorkLevelCost();
    verify(functions).rename();
    verify(functions).setItemPriorWork();
    verify(functions).repairWithMaterial();
    verify(functions, never()).repairWithCombine();
    verify(functions).combineEnchantsJava();
  }

}
