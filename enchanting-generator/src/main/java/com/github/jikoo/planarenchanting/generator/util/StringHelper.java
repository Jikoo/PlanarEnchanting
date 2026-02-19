package com.github.jikoo.planarenchanting.generator.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringHelper {

  public static String toCamel(String input) {
    Pattern snek = Pattern.compile("(?<=[a-z])_([a-z])");
    Matcher matcher = snek.matcher(input);
    return matcher.replaceAll(result -> result.group(1).toUpperCase(Locale.ROOT));
  }

  private StringHelper() {}

}
