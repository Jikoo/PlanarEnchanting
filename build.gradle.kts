import java.lang.Runtime
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  `java-library`
  jacoco
}

repositories {
  mavenCentral()
}

subprojects {
  apply(plugin = "java-library")

  repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io/")
  }

  tasks.withType<JavaCompile>().configureEach {
    options.release = 21
    options.encoding = Charsets.UTF_8.name()
    options.isFork = true
  }
  tasks.withType<Javadoc>().configureEach {
    options.encoding = Charsets.UTF_8.name()
  }
  tasks.withType<ProcessResources>().configureEach {
    filteringCharset = Charsets.UTF_8.name()
  }

  if ("enchanting-generator" != this.name) {
    apply(plugin = "jacoco")

    val mockitoAgent: Configuration = configurations.create("mockitoAgent")
    dependencies {
      compileOnly(rootProject.libs.org.jspecify.jspecify)
      compileOnly(rootProject.libs.org.jetbrains.annotations)

      testCompileOnly(rootProject.libs.org.jspecify.jspecify)
      testImplementation(rootProject.libs.org.hamcrest.hamcrest)
      testImplementation(rootProject.libs.org.mockito.mockito.core)
      mockitoAgent(rootProject.libs.org.mockito.mockito.core) { isTransitive = false }
      testImplementation(rootProject.libs.com.jparams.to.string.verifier)
    }

    testing {
      suites {
        named<JvmTestSuite>("test") {
          useJUnitJupiter("6.0.2")
        }
      }
    }

    tasks.withType<Test>().configureEach {
      // Use as many cores as possible to run tests.
      maxParallelForks = Runtime.getRuntime().availableProcessors()
      // As Bukkit is very heavily statically initialized, don't reuse forks.
      forkEvery = 1
      jvmArgs("-Xshare:off", "-javaagent:${mockitoAgent.asPath}")
      testLogging {
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
        events(TestLogEvent.STANDARD_OUT)
      }
      finalizedBy(tasks.jacocoTestReport)
    }

    tasks.jacocoTestReport {
      enabled = true
      reports {
        // Produce XML report for Sonar
        xml.required = true
      }
    }
  }
}

tasks.jacocoTestReport {
//  executionData.setFrom(subprojects.jacocoTestReport.executionData)
//  executionData.setFrom(files(subprojects.map { executionData }))
}
