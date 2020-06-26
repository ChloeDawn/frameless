plugins {
  id("fabric-loom") version "0.4.28"
  id("signing")
}

group = "dev.sapphic"
version = "1.1.0"

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = sourceCompatibility
}

minecraft {
  refmapName = "mixins/frameless/refmap.json"
  runDir = "run"
}

dependencies {
  minecraft("com.mojang:minecraft:1.16.1")
  mappings("net.fabricmc:yarn:1.16.1+build.5:v2")
  modImplementation("net.fabricmc:fabric-loader:0.8.8+build.202")
  implementation("org.checkerframework:checker-qual:3.4.1")
}

tasks.withType<ProcessResources> {
  filesMatching("/fabric.mod.json") {
    expand("version" to version)
  }
}

tasks.withType<JavaCompile> {
  with(options) {
    isFork = true
    isDeprecation = true
    encoding = "UTF-8"
    compilerArgs.addAll(listOf(
      "-Xlint:all", "-parameters"
    ))
  }
}

signing {
  useGpgCmd()
  sign(configurations.archives.get())
}
