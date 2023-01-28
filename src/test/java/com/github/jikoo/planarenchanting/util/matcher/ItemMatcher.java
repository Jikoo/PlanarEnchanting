package com.github.jikoo.planarenchanting.util.matcher;

import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ItemMatcher {

  /**
   * Construct a new {@code IsSimilarMatcher} for the given {@link ItemStack}.
   *
   * @param other the matchable item
   * @return the resulting matcher
   */
  public static BaseMatcher<ItemStack> isSimilar(@NotNull ItemStack other) {
    return new IsSimilarMatcher(other);
  }

  private static class IsSimilarMatcher extends BaseMatcher<ItemStack> {

    private final @NotNull ItemStack other;

    private IsSimilarMatcher(@NotNull ItemStack other) {
      this.other = other;
    }

    @Override
    public boolean matches(@Nullable Object actual) {
      return actual instanceof ItemStack actualItem && other.isSimilar(actualItem);
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(other.toString());
    }

  }

  /**
   * Construct a new {@code ItemIsEqualMatcher} for the given {@link ItemStack}.
   *
   * @param other the matchable item
   * @return the resulting matcher
   */
  public static BaseMatcher<ItemStack> isItemEqual(@NotNull ItemStack other) {
    return new ItemIsEqualMatcher(other);
  }

  private static class ItemIsEqualMatcher extends BaseMatcher<ItemStack> {
    // TODO can probably delete

    private final @NotNull ItemStack other;
    private final @Nullable BaseMatcher<ItemMeta> metaIsEqualMatcher;

    private ItemIsEqualMatcher(@NotNull ItemStack other) {
      this.other = other;
      if (this.other.hasItemMeta()) {
        this.metaIsEqualMatcher = isMetaEqual(Objects.requireNonNull(other.getItemMeta()));
      } else {
        this.metaIsEqualMatcher = null;
      }
    }

    @Override
    public boolean matches(@Nullable Object actual) {
      if (!(actual instanceof ItemStack actualItem) || !other.equals(actualItem)) {
        return false;
      }

      // If meta is null, equality is valid. Respect special matching otherwise.
      return metaIsEqualMatcher == null || metaIsEqualMatcher.matches(actualItem.getItemMeta());
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(other.toString());
    }

  }

  /**
   * Construct a new {@code MetaIsEqualMatcher} for the given {@link ItemMeta}.
   *
   * @param other the matchable meta
   * @return the resulting matcher
   */
  public static BaseMatcher<ItemMeta> isMetaEqual(@NotNull ItemMeta other) {
    return new MetaIsEqualMatcher(other);
  }

  private static class MetaIsEqualMatcher extends BaseMatcher<ItemMeta> {

    private final @NotNull ItemMeta other;

    private MetaIsEqualMatcher(@NotNull ItemMeta other) {
      this.other = other;
    }

    @Override
    public boolean matches(@Nullable Object actual) {
      return actual instanceof ItemMeta actualMeta && Bukkit.getItemFactory().equals(other, actualMeta);
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(other.toString());
    }

  }

}
