plugins {
    id("net.neoforged.moddev")
    id("neoforge-mutex")
    id("me.modmuss50.mod-publish-plugin")
}

val minecraftVersion = stonecutter.current.version
val neoForgeVersion = property("deps.neoforge") as String
val requiredJava: JavaVersion = when {
    stonecutter.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    stonecutter.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    stonecutter.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    stonecutter.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

version = "${property("mod.version")}+$minecraftVersion-neoforge"
group = property("mod.group") as String
base.archivesName = property("mod.id") as String

neoForge {
    version = neoForgeVersion

    runs {
        create("client") {
            client()
            gameDirectory = rootProject.file("run")
        }
        create("server") {
            server()
            gameDirectory = rootProject.file("run")
        }
    }

    mods.create(property("mod.id") as String) {
        sourceSet(sourceSets.main.get())
    }
}

java {
    withSourcesJar()
    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
    sourceCompatibility = requiredJava
    targetCompatibility = requiredJava
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(requiredJava.majorVersion.toInt())
}

tasks {
    processResources {
        fun MutableMap<String, String>.register(key: String, property: String) {
            val value: String = stonecutter.properties[property]
            inputs.property(key, value)
            set(key, value)
        }

        val props = buildMap {
            register("version", "mod.version")
            register("mc", "mod.neoforge_mc_range")
            register("neoforge", "deps.neoforge")
            register("modName", "mod.name")
            register("modId", "mod.id")
            register("modDescription", "mod.description")
            register("authors", "mod.authors")
            register("license", "mod.license")
            register("homepage", "mod.homepage")
            register("issues", "mod.issues")
            register("sources", "mod.sources")
        }
        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("META-INF/neoforge.mods.toml") { expand(props) }
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }
        exclude("fabric.mod.json", "META-INF/mods.toml")
        if (stonecutter.eval(stonecutter.current.version, ">=1.21")) {
            exclude("data/*/loot_table/**")
        } else {
            exclude("data/*/loot_tables/**")
        }
        if (minecraftVersion.startsWith("1.")) {
            exclude("assets/*/items/**")
        }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"
        inputs.property("version", project.property("mod.version"))
        from(jar.flatMap { it.archiveFile }, named<Jar>("sourcesJar").flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }
}

val compatibleVersions = stonecutter.properties.rawOrNull("mod.mc_releases")
    ?.asList().orEmpty().map { it.toString() }

publishMods {
    val modrinthToken = System.getenv("MODRINTH_TOKEN")
    val curseForgeToken = System.getenv("CURSEFORGE_TOKEN")

    file = tasks.named<Jar>("jar").get().archiveFile
    dryRun = modrinthToken == null || curseForgeToken == null
    displayName = "${property("mod.name")} ${property("mod.version")} - Neoforge ${minecraftVersion}"
    version = project.version.toString()
    changelog = rootProject.file("CHANGELOG.md").readText()
    type = STABLE
    modLoaders.add("neoforge")
    curseforge {
        projectId = property("publish.curseforge").toString()
        accessToken = curseForgeToken ?: ""
        compatibleVersions.forEach { minecraftVersions.add(it) }
        client = true
        server = true
    }
    modrinth {
        projectId = property("publish.modrinth").toString()
        accessToken = modrinthToken
        compatibleVersions.forEach { minecraftVersions.add(it) }
        environment = CLIENT_OR_SERVER
    }
}
