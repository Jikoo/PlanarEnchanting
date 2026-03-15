package com.github.jikoo.planarenchanting.anvil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorkPieceTest {

  private ViewState<Void> state;
  private Temperer<Void> temperer;

  @BeforeEach
  void setUp() {
    state = mock();
    temperer = mock();
  }

  @Test
  void applyFalse() {
    AnvilFunction<Void> function = mock();
    WorkPiece<Void> piece = new WorkPiece<>(state, temperer);

    assertThat(
        "AnvilFunction that cannot apply results in no changes",
        piece.apply(mock(), function),
        is(false)
    );

    verify(function, never()).getResult(any(), any(), any());
  }

  @Test
  void applyTrue() {
    AnvilFunction<Void> function = mock();
    doReturn(true).when(function).canApply(any(), any(), any());
    AnvilFunctionResult<Void> result = mock();
    doReturn(result).when(function).getResult(any(), any(), any());
    WorkPiece<Void> piece = new WorkPiece<>(state, temperer);

    assertThat(
        "AnvilFunction that can apply is applied",
        piece.apply(mock(), function),
        is(true)
    );

    verify(function).getResult(any(), any(), any());
    verify(result).modifyResult(any());
  }

  @Test
  void temperChanged() {
    doReturn(true).when(temperer).hasChanged(any(), any(), any());
    WorkPiece<Void> workPiece = new WorkPiece<>(state, temperer);

    assertThat(
        "Changed work piece does not result in empty result",
        workPiece.temper(),
        is(not(sameInstance(AnvilResult.EMPTY)))
    );
  }

  @Test
  void temperUnchanged() {
    WorkPiece<Void> workPiece = new WorkPiece<>(state, temperer);

    assertThat(
        "Unchanged work piece produces empty result",
        workPiece.temper(),
        is(AnvilResult.EMPTY)
    );
  }

}
