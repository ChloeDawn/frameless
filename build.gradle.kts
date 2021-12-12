import java.time.Instant

plugins {
  id("fabric-loom") version "0.10.64"
  id("net.nemerosa.versioning") version "2.15.1"
  id("signing")
}

group = "dev.sapphic"
version = "1.2.0"

java {
  withSourcesJar()
}

loom {
  mixin {
    defaultRefmapName.set("mixins/frameless/refmap.json")
  }

  runs {
    configureEach {
      property("mixin.debug", "true")
      property("mixin.debug.export.decompile", "false")
      property("mixin.debug.verbose", "true")
      property("mixin.dumpTargetOnFailure", "true")
      property("mixin.checks", "true")
      property("mixin.hotSwap", "true")
    }
  }
}

repositories {
  maven("https://maven.terraformersmc.com/releases") {
    content {
      includeGroup("com.terraformersmc")
    }
  }
}

dependencies {
  minecraft("com.mojang:minecraft:1.17.1")
  mappings(loom.officialMojangMappings())
  modImplementation("net.fabricmc:fabric-loader:0.12.11")
  implementation("org.jetbrains:annotations:23.0.0")
  implementation("org.checkerframework:checker-qual:3.20.0")
  modRuntimeOnly("com.terraformersmc:modmenu:2.0.3")
}

tasks {
  compileJava {
    with(options) {
      release.set(16)
      isFork = true
      isDeprecation = true
      encoding = "UTF-8"
      compilerArgs.addAll(listOf("-Xlint:all", "-parameters"))
    }
  }

  processResources {
    filesMatching("/fabric.mod.json") {
      expand("version" to project.version)
    }
  }

  jar {
    from("/LICENSE")

    manifest.attributes(
      "Build-Timestamp" to Instant.now(),
      "Build-Revision" to versioning.info.commit,
      "Build-Jvm" to "${
        System.getProperty("java.version")
      } (${
        System.getProperty("java.vendor")
      } ${
        System.getProperty("java.vm.version")
      })",
      "Built-By" to GradleVersion.current(),

      "Implementation-Title" to project.name,
      "Implementation-Version" to project.version,
      "Implementation-Vendor" to project.group,

      "Specification-Title" to "FabricMod",
      "Specification-Version" to "1.0.0",
      "Specification-Vendor" to project.group,

      "Sealed" to "true"
    )
  }

  if (hasProperty("signing.mods.keyalias")) {
    val alias = property("signing.mods.keyalias")
    val keystore = property("signing.mods.keystore")
    val password = property("signing.mods.password")

    listOf(remapJar, remapSourcesJar).forEach {
      it.get().doLast {
        if (!project.file(keystore!!).exists()) {
          error("Missing keystore $keystore")
        }

        val file = outputs.files.singleFile
        ant.invokeMethod(
          "signjar", mapOf(
            "jar" to file,
            "alias" to alias,
            "storepass" to password,
            "keystore" to keystore,
            "verbose" to true,
            "preservelastmodified" to true
          )
        )
        signing.sign(file)
      }
    }
  }

  assemble {
    dependsOn(versionFile)
  }
}

