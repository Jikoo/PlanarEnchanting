repositories {
  maven("https://hub.spigotmc.org/nexus/content/groups/public/")
}

dependencies {
  compileOnly(libs.org.spigotmc.spigot.api)
  implementation(project(":enchanting-common"))

  testImplementation(libs.org.spigotmc.spigot.api)
}
