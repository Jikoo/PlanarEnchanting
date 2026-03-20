package com.github.jikoo.planarenchanting.anvil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

class MetaCachedStackTest {

  @Test
  void nullIsAir() {
    MetaCachedStack stack = new MetaCachedStack(null);
    assertThat("Null item is air", stack.getItem().getType(), is(Material.AIR));
  }

  @Test
  void metaIsCached() {
    ItemStack itemStack = mock();
    MetaCachedStack stack = new MetaCachedStack(itemStack);

    stack.getMeta();
    verify(itemStack).getItemMeta();
    stack.getMeta();
    verifyNoMoreInteractions(itemStack);
  }

}
