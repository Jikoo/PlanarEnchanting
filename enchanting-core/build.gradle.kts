plugins {
  jacoco
  alias(libs.plugins.org.sonarqube)
}

repositories {
  maven("https://repo.papermc.io/repository/maven-public/")
  maven("https://jitpack.io/")
}

val mockitoAgent: Configuration = configurations.create("mockitoAgent")

dependencies {
  implementation(libs.org.jetbrains.annotations)
  implementation(libs.io.papermc.paper.paper.api)
  implementation(libs.com.github.jikoo.planarwrappers)

  testImplementation(libs.org.hamcrest.hamcrest)
  testImplementation(libs.org.mockito.mockito.core)
  mockitoAgent(libs.org.mockito.mockito.core) { isTransitive = false }
  testImplementation(libs.com.jparams.to.string.verifier)
}

sourceSets {
  main {
    java.srcDirs("src/generated/java")
  }
}

testing {
  suites {
    named<JvmTestSuite>("test") {
      useJUnitJupiter("6.0.2")
    }
  }
}

tasks.test {
  useJUnitPlatform()
  // As Bukkit is very heavily statically initialized, don't reuse forks.
  forkEvery = 1
  jvmArgs("-javaagent:${mockitoAgent.asPath}")
  // Generate coverage reports
  finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
  reports {
    // Produce XML report for Sonar
    xml.required = true
  }
}

sonar {
  properties {
    property("sonar.projectKey", "Jikoo_PlanarEnchanting")
    property("sonar.organization", "jikoo")
    property("sonar.host.url", "https://sonarcloud.io")
    property("sonar.language", "java")
    property("sonar.java.coveragePlugin", "jacoco")
    property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
  }
}
