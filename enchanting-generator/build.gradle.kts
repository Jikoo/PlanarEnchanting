import org.gradle.kotlin.dsl.register

repositories {
  maven("https://repo.papermc.io/repository/maven-public/")
  maven("https://jitpack.io/")
}

plugins {
  alias(libs.plugins.io.papermc.paperweight)
}

dependencies {
  implementation(libs.com.palantir.javapoet.javapoet)
  implementation(libs.io.papermc.paper.paper.api)
  paperweight.paperDevBundle(libs.versions.io.papermc.paper.paper.api)
}

var core: Project = project(":planarenchanting")
var generationDir: Directory = core.layout.projectDirectory.dir("src/generated/java")

val generate = tasks.register<JavaExec>("generate") {
  dependsOn("removeGeneratedFiles", "build")

  mainClass = "com.github.jikoo.planarenchanting.generator.Main"
  classpath = sourceSets.main.get().compileClasspath.plus(sourceSets.main.get().runtimeClasspath)
  args(generationDir.toString())

  finalizedBy("removeGeneratedLogs")
}

tasks.register<Delete>("removeGeneratedFiles") {
  delete(generationDir)
}

tasks.register<Delete>("removeGeneratedLogs") {
  // Minecraft server bootstrap generates logs.
  // It's a lot easier to just remove them after the fact than prevent their creation.
  delete(layout.projectDirectory.dir("logs"))
}

core.tasks.named<JavaCompile>("compileJava") {
  // Require compilation to wait on generation to complete if run at the same time.
  mustRunAfter(generate)
}
