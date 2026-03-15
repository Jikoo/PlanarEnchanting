dependencies {
  compileOnly(libs.io.papermc.paper.paper.api)
  implementation(project(":enchanting-common")) {
    exclude(group = "org.spigotmc", module = "spigot-api")
  }

  testImplementation(libs.io.papermc.paper.paper.api)
}
