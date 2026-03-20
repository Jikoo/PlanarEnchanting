package com.github.jikoo.planarenchanting.anvil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.github.jikoo.planarenchanting.util.mock.ServerMocks;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ComponentTempererTest {

  @BeforeAll
  void setUp() {
    // DataComponentTypes are fetched from the server registry.
    ServerMocks.mockServer();
    // Touch to initialize.
    DataComponentTypes.REPAIR_COST.key();
  }

  @Test
  void hasChangedEmpty() {
    ItemStack base = mock();
    doReturn(true).when(base).isEmpty();
    ItemStack addition = mock();
    ItemStack result = mock();
    doReturn(true).when(result).isEmpty();

    assertThat(
        "Empty base and result are not changed",
        ComponentTemperer.INSTANCE.hasChanged(base, addition, result),
        is(false)
    );
  }

  @Test
  void hasChangedEmptyBase() {
    ItemStack base = mock();
    doReturn(true).when(base).isEmpty();
    ItemStack addition = mock();
    ItemStack result = mock();

    assertThat(
        "Empty base is not changed",
        ComponentTemperer.INSTANCE.hasChanged(base, addition, result),
        is(false)
    );
  }

  @Test
  void hasChangedEmptyResult() {
    ItemStack base = mock();
    ItemStack addition = mock();
    ItemStack result = mock();
    doReturn(true).when(result).isEmpty();

    assertThat(
        "Empty result is not changed",
        ComponentTemperer.INSTANCE.hasChanged(base, addition, result),
        is(false)
    );
  }

  @Test
  void hasChanged() {
    ItemStack base = mock();
    ItemStack addition = mock();
    ItemStack result = mock();
    doReturn(result).when(result).clone();

    assertThat(
        "Different result is changed",
        ComponentTemperer.INSTANCE.hasChanged(base, addition, result),
        is(true)
    );
  }

  @Test
  void hasChangedUnchanged() {
    ItemStack base = mock();
    ItemStack addition = mock();
    doReturn(true).when(addition).isEmpty();
    ItemStack result = mock();
    // Since we can't reimplement .equals for the return value,
    // we can just make the "copy" be the original.
    doReturn(base).when(result).clone();

    assertThat(
        "Unchanged result is not changed",
        ComponentTemperer.INSTANCE.hasChanged(base, addition, result),
        is(false)
    );
  }

  @Test
  void hasChangedResetAndSet() {
    ItemStack base = mock();
    doReturn(5).when(base).getData(DataComponentTypes.REPAIR_COST);
    ItemStack addition = mock();
    ItemStack result = mock();
    doReturn(result).when(result).clone();

    assertThat(
        "Different result is changed",
        ComponentTemperer.INSTANCE.hasChanged(base, addition, result),
        is(true)
    );

    verify(result).setData(DataComponentTypes.REPAIR_COST, 5);
    verify(result).resetData(DataComponentTypes.CUSTOM_NAME);
  }

  @Test
  void temper() {
    ItemStack result = mock();
    assertThat(
        "Temper is a no-op",
        ComponentTemperer.INSTANCE.temper(result),
        is(result)
    );
    verifyNoInteractions(result);
  }

}
