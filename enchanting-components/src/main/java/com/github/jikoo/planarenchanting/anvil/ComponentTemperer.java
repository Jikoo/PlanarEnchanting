package com.github.jikoo.planarenchanting.anvil;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

/**
 * A {@link Temperer} for {@link DataComponentType DataComponent}-based operations.
 */
@NullMarked
public class ComponentTemperer implements Temperer<ItemStack> {

  public static final ComponentTemperer INSTANCE = new ComponentTemperer();

  @Override
  public boolean hasChanged(ItemStack base, ItemStack addition, ItemStack result) {
    if (base.isEmpty() || result.isEmpty()) {
      return false;
    }

    // Prepare to check if the item has changed and there is a real result.
    // As we want to conditionally ignore certain changes, we need to copy the result.
    ItemStack modResult = result.clone();
    // Ignore changes to repair cost - these should generally always happen.
    resetData(modResult, base, DataComponentTypes.REPAIR_COST);
    // Ignore custom name if the addition is not empty.
    if (!addition.isEmpty()) {
      resetData(modResult, base, DataComponentTypes.CUSTOM_NAME);
    }

    return !base.equals(modResult);
  }

  @Override
  public ItemStack temper(ItemStack result) {
    return result;
  }

  private <T> void resetData(ItemStack reset, ItemStack original, DataComponentType.Valued<T> type) {
    T data = original.getData(type);
    if (data == null) {
      reset.resetData(type);
    } else {
      reset.setData(type, data);
    }
  }

  private ComponentTemperer() {}

}
