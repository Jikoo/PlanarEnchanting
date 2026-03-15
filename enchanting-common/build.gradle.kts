repositories {
  maven("https://hub.spigotmc.org/nexus/content/groups/public/")
}

dependencies {
  compileOnly(libs.org.spigotmc.spigot.api)
  implementation(libs.com.github.jikoo.planarwrappers)

  testImplementation(libs.org.spigotmc.spigot.api)
}

sourceSets {
  main {
    java.srcDirs("src/generated/java")
  }
}
