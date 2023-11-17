spotless {
  format 'misc', {
    // define the files to apply `misc` to
    target '*.gradle', '*.md', '.gitignore'

    // define the steps to apply to those files
    trimTrailingWhitespace()
    indentWithSpaces()
    endWithNewline()
  }
  java {
    target 'src/*/java/**/*.java'
    removeUnusedImports()

    // apply a specific flavor of google-java-format
    googleJavaFormat('1.18.1')
      .aosp()
      .reflowLongStrings()
      .skipJavadocFormatting()
      .reorderImports(true)

    // fix formatting of type annotations
    formatAnnotations().addTypeAnnotation("SuppressWarnings").addTypeAnnotation("Nonnull")
  }
}
