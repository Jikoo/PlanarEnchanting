plugins {
  alias(libs.plugins.shadow)
  `maven-publish`
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

tasks.jar {
  enabled = false
}

tasks.shadowJar {
  dependsOn("classes")
  archiveClassifier.set("")

  archiveBaseName = "${project.name}"
}

tasks.assemble {
  dependsOn(tasks.shadowJar)
}

artifacts {
  add("default", tasks.shadowJar)
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["shadow"])
    }
  }
  repositories {
    mavenLocal()
  }
}
