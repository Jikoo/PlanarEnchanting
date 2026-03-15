package com.github.jikoo.planarenchanting.anvil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ComponentViewStateTest {

  private MockedStatic<ItemStack> itemStack;

  @BeforeAll
  void setUp() {
    itemStack = mockStatic(ItemStack.class);
    itemStack.when(ItemStack::empty).thenAnswer(invocation -> {
      ItemStack stack = mock();
      doReturn(true).when(stack).isEmpty();
      return stack;
    });
  }

  @AfterAll
  void tearDown() {
    itemStack.close();
  }

  @Test
  void getOriginalView() {
    AnvilView view = mock();
    ComponentViewState state = new ComponentViewState(view);
    assertThat("Original view is available", state.getOriginalView(), is(view));
  }

  @Test
  void getBase() {
    AnvilView view = mock();
    ItemStack stack = mock();
    doReturn(stack).when(view).getItem(0);

    ComponentViewState state = new ComponentViewState(view);

    assertThat("Base is expected value", state.getBase(), is(stack));
  }

  @Test
  void getBaseNull() {
    AnvilView view = mock();

    ComponentViewState state = new ComponentViewState(view);

    ItemStack result = state.getBase();
    assertThat("Base is not null", result, is(notNullValue()));
    assertThat("Base is empty", result.isEmpty(), is(true));
  }

  @Test
  void getAddition() {
    AnvilView view = mock();
    ItemStack stack = mock();
    doReturn(stack).when(view).getItem(1);

    ComponentViewState state = new ComponentViewState(view);

    assertThat("Addition is expected value", state.getAddition(), is(stack));
  }

  @Test
  void getAdditionNull() {
    AnvilView view = mock();

    ComponentViewState state = new ComponentViewState(view);

    ItemStack result = state.getAddition();
    assertThat("Addition is not null", result, is(notNullValue()));
    assertThat("Addition is empty", result.isEmpty(), is(true));
  }

  @Test
  void createResult() {
    ItemStack stack = mock();
    doAnswer(invocation -> mock(ItemStack.class)).when(stack).clone();
    AnvilView view = mock();
    doReturn(stack).when(view).getItem(0);

    ComponentViewState state = new ComponentViewState(view);

    assertThat("Base is expected value", state.getBase(), is(stack));
    assertThat("Result is not base", state.createResult(), is(not(sameInstance(stack))));
  }

}
