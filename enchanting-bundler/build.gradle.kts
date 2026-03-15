plugins {
  alias(libs.plugins.shadow)
}

dependencies {
  compileOnly(libs.org.spigotmc.spigot.api)
  implementation(libs.com.github.jikoo.planarwrappers)
  implementation(project(":enchanting-common"))
  implementation(project(":enchanting-components")) {
    exclude(group = "io.papermc.paper", module = "paper-api")
  }
  implementation(project(":enchanting-meta"))

  testImplementation(libs.org.spigotmc.spigot.api)
}

tasks.shadowJar {
  dependsOn(tasks.jar)
  // TODO wrappers not included, may need to bundle in common
  relocate("com.github.jikoo.planarwrappers", "com.github.jikoo.planarenchanting.lib.planarwrappers")
  minimize()

  archiveBaseName = "${project.name}"
}

