repositories {
  maven("https://hub.spigotmc.org/nexus/content/groups/public/")
}

dependencies {
  compileOnly(libs.org.spigotmc.spigot.api)
  implementation(project(":enchanting-common")) {
    exclude(group = "com.github.jikoo", module = "planarwrappers")
  }

  testImplementation(libs.org.spigotmc.spigot.api)
}
