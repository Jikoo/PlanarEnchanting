import java.lang.Runtime
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

subprojects {
  apply(plugin = "java-library")

  repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
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

  tasks.withType<Test>().configureEach {
    // Use as many cores as possible to run tests.
    maxParallelForks = Runtime.getRuntime().availableProcessors()
    jvmArgs("-Xshare:off")
    testLogging {
      showStackTraces = true
      exceptionFormat = TestExceptionFormat.FULL
      events(TestLogEvent.STANDARD_OUT)
    }
  }
}
