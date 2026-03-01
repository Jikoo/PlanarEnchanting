package com.github.jikoo.planarenchanting.generator;

import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeSpec;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import javax.annotation.processing.Generated;
import net.minecraft.SharedConstants;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class Generator {

  protected final ClassName generatedClass;
  protected TypeSpec.@UnknownNullability Builder builder;

  public Generator(String pkg, String name) {
    this.generatedClass = ClassName.get(pkg, name);
  }

  public void buildSpec() {
    if (builder != null) {
      return;
    }
    builder = create()
        .addJavadoc(
            "<p>This file was generated from Minecraft $L. Regenerate it rather than modify.</p>",
            SharedConstants.getCurrentVersion().name()
        )
        .addAnnotation(
            AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", getClass().getName())
                .build()
        )
        .addAnnotation(NullMarked.class);
  }

  protected abstract TypeSpec.Builder create();

  public void generate(Path dir) throws IOException {
    buildSpec();

    if (builder == null) {
      throw new IllegalStateException();
    }

    StringBuilder stringBuilder = new StringBuilder();
    JavaFile.builder(generatedClass.packageName(), builder.build())
        .indent("  ")
        .skipJavaLangImports(true)
        .build()
        .writeTo(stringBuilder);
    String content = tweakFormatting(stringBuilder.toString());

    Path destDir = dir.resolve(generatedClass.packageName().replace('.', '/'));
    Files.createDirectories(destDir);

    Path output = destDir.resolve(generatedClass.simpleName() + ".java");

    try (Writer writer = new OutputStreamWriter(Files.newOutputStream(output), StandardCharsets.UTF_8)) {
      writer.append(content);
    }

  }

  protected String tweakFormatting(String content) {
    // Attach comment hacks to subsequent line.
    content = content.replaceAll("( {2}//.*?);\n\n", "$1\n");

    // For fields, JavaPoet generates 2 intermediary newlines, which looks pretty ugly.
    // Group matching visibility and scope fields by removing the second newline.
    content = SEPARATED_ALIKE_CLASS_FIELDS.matcher(content).replaceAll("${field}");

    // Inline nullity annotations.
    // Hopefully there aren't multiple nullity annotations! We could re-run a matcher for this
    // pattern over the result until one doesn't match, but that's probably overkill.
    content = SEPARATE_LINE_NULLITY_ANNOTATION.matcher(content).replaceAll("${prefix}${annotation} ");

    // Inline single-line Javadocs.
    content = content.replaceAll(
        "(?<docOpen>\\s+/\\*\\*)\n\\s+(?:\\* )?(?<content>.*)\n\\s+\\*/",
        "${docOpen} ${content} */"
    );

    // Inline empty bodies.
    content = content.replaceAll("\\{\n\\s*?}\n", "{}\n");

    // I like blank lines at the top and bottom of classes.
    // Top allows for empty enum declaration.
    content = content.replaceAll("(\n\\S.*\\{\n(\\s+;\n)?)", "$1\n");
    content = content.replace("\n}", "\n\n}");

    return content;
  }

  /**
   * Lenient annotation declaration. This is a bit hairy, because annotations may have values set.
   */
  private static final @RegExp String ANNOTATION = "@[\\w.]+(?:\\(.*?\\))?";
  /** Lenient generic type declaration. */
  private static final @RegExp String GENERIC = "<(?:(?:" + ANNOTATION + " )*?[\\w<>, ]+)+>";
  /**
   * Field/variable declaration.
   * Word boundary followed by alphanumeric string with optional assignment.
   * Must end in a semicolon at the end of a line.
   */
  private static final @RegExp String FIELDLIKE = ".*\\b\\w+(?: = .*?)?;\n";
  /** Type name. Includes non-capturing optional generics. */
  private static final @RegExp String TYPE_NAME = "\\w+(?:" + GENERIC + ")?";
  /** Type + non-capturing optional array(s) + name */
  private static final @RegExp String TYPE_DECLARATION = TYPE_NAME + "(?: ?\\[])*? " + "\\w+";
  /**
   * Non-capturing group repeated 0 or more times containing keywords or generic declarations.
   */
  private static final @RegExp String METHOD_AND_FIELD_PREFIXES =
      "(?:(?:public|protected|private|abstract|static|final|" + GENERIC + "|" +  ANNOTATION + ") )*?";

  /**
   * A regular expression used to replace double newlines between field declarations. The named
   * group {@code field} captures the whole declaration including newline.
   *
   * <p><b>This implementation is fragile.</b> It is designed for use with code generated using
   * a specific builder configuration. There are several assumptions made about ordering, and
   * detection rules are looser than a real parser for ease of readability and expression.</p>
   */
  @SuppressWarnings("RegExpSuspiciousBackref") // Analysis confused by named groups inside other groups. Works fine.
  private static final Pattern SEPARATED_ALIKE_CLASS_FIELDS = Pattern.compile(
      // Open a named group for the whole field.
      "^(?<field>"
          // Indent will always be depth of 1 (2 spaces) because these are class-level fields.
          // Fields in code blocks can just be declared in code to prevent ugly newlines; this
          // is the only time the field emitter actually has to be used.
          + " {2}"
          // Capture the visibility; We want whitespace between different visibilities for clarity.
          + "(?<visibility>(?:public|protected|private) )?"
          // Capture the scope for the same reason.
          + "(?<static>static )?"
          // Require the line to be field-like.
          + FIELDLIKE
          // Finish named group.
          + ")"
          // The entire problem we're here to solve: a single trailing newline. It's worth it, trust me.
          + "\n"
          // Start lookahead for subsequent field.
          // Subsequent field is required because we want to leave a whitespace line before a
          // constructor declaration, static block, or method call.
          + "(?="
          // Javadoc comments. Multi-line only, because JavaPoet only generates multi-line Javadocs.
          + "(?: {2}/\\*\\*\n(?:.*\n)*?\\s*\\*/\n)?"
          // Regular comments in case of comment hack fields.
          + "(?: {2}//.*?\n)?"
          // Any annotations.
          + "(?: {2}" + ANNOTATION + "\n)*?"
          // Matching indent.
          + " {2}"
          // Matching visibility and scope using backrefs to named groups from first field.
          + "\\k<visibility>\\k<static>"
          // Require the line to be fieldlike.
          + FIELDLIKE
          // End lookahead.
          + ")",
      Pattern.MULTILINE
  );

  /**
   * A regular expression used to inline nullity annotations. Captures groups {@code prefix} and {@code annotation}.
   */
  private static final Pattern SEPARATE_LINE_NULLITY_ANNOTATION = Pattern.compile(
      // Named group for annotation: @ + optional package + nullity class name variants
      "^\\s+(?<annotation>@.*(?:Nullable|No[tn][Nn]ull))\n"
          // Named group for everything else to keep between the annotation and the type.
          + "(?<prefix>"
          // Any other annotations.
          + "(?:\\s+" + ANNOTATION + ")*"
          // Indentation and modifiers/generics.
          + "\\s+" + METHOD_AND_FIELD_PREFIXES
          // End of prefix group.
          + ")"
          // Lookahead for type.
          + "(?=" + TYPE_DECLARATION + ")",
      Pattern.MULTILINE
  );

}
