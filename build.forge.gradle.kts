plugins {
    id("net.minecraftforge.gradle") version ("6.0.46")
    id("org.spongepowered.mixin") version ("0.7.+")
    id("org.parchmentmc.librarian.forgegradle") version ("1.+")
    id("maven-publish")
}

version = "${property("mod.version")}+${property("deps.minecraft")}-forge"
base.archivesName = property("mod.id") as String

minecraft {
    mappings("parchment", "${property("deps.parchment")}-${property("deps.minecraft")}")

    runs {
        create("client") {
            workingDirectory(project.file("run"))
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            mods {
                create(property("mod.id") as String) {
                    source(sourceSets.main.get())
                }
            }
        }

        create("server") {
            workingDirectory(project.file("run"))
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            mods {
                create(property("mod.id") as String) {
                    source(sourceSets.main.get())
                }
            }
        }

        create("data") {
            workingDirectory(project.file("run"))
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            args("--mod", property("mod.id") as String, "--all", "--output", file("src/generated/resources/"), "--existing", file("src/main/resources/"))
            mods {
                create(property("mod.id") as String) {
                    source(sourceSets.main.get())
                }
            }
        }
    }
}

repositories {
    maven("https://maven.parchmentmc.org")
    mavenCentral()
}


dependencies {
    minecraft("net.minecraftforge:forge:${property("deps.minecraft")}-${property("deps.forge_version")}")

    implementation("com.google.code.gson:gson:2.10.1")

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
}

tasks.named<ProcessResources>("processResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    val props = mapOf(
        "version" to project.version,
        "mc" to project.property("deps.minecraft"),
        "modName" to project.property("mod.name"),
        "modId" to project.property("mod.id"),
        "modDescription" to project.property("mod.description"),
        "authors" to project.property("mod.authors"),
        "license" to project.property("mod.license"),
        "forge" to project.property("deps.forge_version"),
    )

    inputs.properties(props)

    filesMatching("META-INF/mods.toml") {
        expand(props)
    }

    exclude("**/fabric.mod.json", "**/*.accesswidener")
}

stonecutter {
    val loaderClientField = "@net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)"
    val stringReplacements = mapOf(
        "@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)" to loaderClientField
    )

    stringReplacements.forEach { (from, to) ->
        replacements.string {
            direction = true
            replace(from, to)
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    val javaVersion = 17
    options.release.set(javaVersion)

    exclude("**/integration/surveyor/**")
}

java {
    withSourcesJar()
    val javaVersion = 17
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
}