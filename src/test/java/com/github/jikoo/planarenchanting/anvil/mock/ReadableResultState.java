package com.github.jikoo.planarenchanting.anvil.mock;

import com.github.jikoo.planarenchanting.anvil.AnvilBehavior;
import com.github.jikoo.planarenchanting.anvil.AnvilState;
import com.github.jikoo.planarenchanting.util.MetaCachedStack;
import org.bukkit.inventory.view.AnvilView;
import org.jetbrains.annotations.NotNull;

public class ReadableResultState extends AnvilState {

  public ReadableResultState(
      @NotNull AnvilBehavior behavior,
      @NotNull AnvilView view) {
    super(behavior, view);
  }

  public @NotNull MetaCachedStack getResult() {
    return this.result;
  }

}
