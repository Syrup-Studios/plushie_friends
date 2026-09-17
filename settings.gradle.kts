pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases/")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.10"
    id("dev.kikugie.loom-back-compat") version "0.3"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "plushie_friends"

stonecutter {
    create(rootProject) {
        fun match(version: String, vararg loaders: String) = loaders
            .forEach { loader -> version("$version-$loader", version).buildscript = "build.$loader.gradle.kts" }

        match("1.20.1", "fabric")
        match("1.21.1", "fabric", "neoforge")
        version("26.2-fabric", "26.2").buildscript = "build.fabric-modern.gradle.kts"
        version("26.2-neoforge", "26.2").buildscript = "build.neoforge.gradle.kts"
        version("26.3-fabric", "26.3").buildscript = "build.fabric-modern.gradle.kts"
        version("26.3-neoforge", "26.3").buildscript = "build.neoforge.gradle.kts"

        vcsVersion = "1.20.1-fabric"
    }
}
