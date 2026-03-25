package com.github.jikoo.planarenchanting.anvil;

import org.bukkit.inventory.view.AnvilView;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class MetaViewState implements ViewState<MetaCachedStack> {

  private final AnvilView view;
  private final MetaCachedStack base;
  private final MetaCachedStack addition;

  public MetaViewState(AnvilView view) {
    this.view = view;
    this.base = new MetaCachedStack(view.getItem(0));
    this.addition = new MetaCachedStack(view.getItem(1));
  }

  @Override
  public AnvilView getAnvilView() {
    return view;
  }

  @Override
  public MetaCachedStack getBase() {
    return base;
  }

  @Override
  public MetaCachedStack getAddition() {
    return addition;
  }

  @Override
  public MetaCachedStack createResult() {
    return new MetaCachedStack(base.getItem().clone());
  }

}
