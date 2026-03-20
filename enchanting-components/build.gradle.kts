dependencies {
  compileOnly(libs.io.papermc.paper.paper.api)
  implementation(project(":enchanting-common")) {
    exclude(group = "com.github.jikoo", module = "planarwrappers")
    exclude(group = "org.spigotmc", module = "spigot-api")
  }

  testImplementation(libs.io.papermc.paper.paper.api)
}
