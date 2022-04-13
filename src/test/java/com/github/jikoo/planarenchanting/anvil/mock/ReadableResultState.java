package com.github.jikoo.planarenchanting.anvil.mock;

import com.github.jikoo.planarenchanting.anvil.AnvilOperation;
import com.github.jikoo.planarenchanting.anvil.AnvilOperationState;
import com.github.jikoo.planarenchanting.util.MetaCachedStack;
import org.bukkit.inventory.AnvilInventory;
import org.jetbrains.annotations.NotNull;

public class ReadableResultState extends AnvilOperationState {

  public ReadableResultState(
      @NotNull AnvilOperation operation,
      @NotNull AnvilInventory inventory) {
    super(operation, inventory);
  }

  public @NotNull MetaCachedStack getResult() {
    return this.result;
  }

}
