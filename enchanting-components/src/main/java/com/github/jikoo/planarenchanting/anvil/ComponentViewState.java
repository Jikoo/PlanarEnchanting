package com.github.jikoo.planarenchanting.anvil;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ComponentViewState implements ViewState<ItemStack> {

  private final AnvilView view;
  private final ItemStack base;
  private final ItemStack addition;

  public ComponentViewState(AnvilView view) {
    this.view = view;
    ItemStack stack = view.getItem(0);
    this.base = stack != null ? stack : ItemStack.empty();
    stack = view.getItem(1);
    this.addition = stack != null ? stack : ItemStack.empty();
  }

  @Override
  public AnvilView getAnvilView() {
    return view;
  }

  @Override
  public ItemStack getBase() {
    return base;
  }

  @Override
  public ItemStack getAddition() {
    return addition;
  }

  @Override
  public ItemStack createResult() {
    return base.clone();
  }

}
