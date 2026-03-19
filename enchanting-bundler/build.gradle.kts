plugins {
  alias(libs.plugins.shadow)
}

dependencies {
  compileOnly(libs.org.spigotmc.spigot.api)
  implementation(project(":enchanting-common", configuration = "shadowRuntimeElements")) {
    exclude(group = "com.github.jikoo", module = "planarwrappers")
  }
  implementation(project(":enchanting-components")) {
    exclude(group = "io.papermc.paper", module = "paper-api")
  }
  implementation(project(":enchanting-meta"))

  testImplementation(libs.org.spigotmc.spigot.api)
}

tasks.shadowJar {
  dependsOn(tasks.jar)

  archiveBaseName = "${project.name}"
}

tasks.build {
  dependsOn(tasks.shadowJar)
}
