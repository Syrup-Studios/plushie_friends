plugins {
    id("dev.kikugie.loom-back-compat")
    id("me.modmuss50.mod-publish-plugin")
}

val minecraftVersion = stonecutter.current.version
val requiredJava: JavaVersion = when {
    stonecutter.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    stonecutter.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    stonecutter.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    stonecutter.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}
val compatibleVersions = stonecutter.properties.rawOrNull("mod.mc_releases")
    ?.asList().orEmpty().map { it.toString() }

version = "${property("mod.version")}+$minecraftVersion-fabric"
base.archivesName = property("mod.id") as String

repositories {
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    loomx.applyMojangMappings()

    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json") // Useful for interface injection

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1") // Adds names to lambdas - useful for mixins
    }

    runConfigs.configureEach {
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = rootProject.file("run")
        jvmArguments.add("-Dmixin.debug.export=true")
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
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
            register("mc", "mod.fabric_mc_range")
            register("modName", "mod.name")
            register("modId", "mod.id")
            register("modDescription", "mod.description")
            register("authors", "mod.authors")
            register("license", "mod.license")
            register("homepage", "mod.homepage")
            register("issues", "mod.issues")
            register("sources", "mod.sources")
            register("fl", "deps.fabric_loader")
            register("fapi", "deps.fabric_api")
            inputs.property("java", requiredJava.majorVersion)
            set("java", requiredJava.majorVersion)
        }

        filesMatching("fabric.mod.json") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }
        exclude("META-INF/neoforge.mods.toml", "META-INF/mods.toml")
        if (stonecutter.eval(stonecutter.current.version, ">=1.21")) {
            exclude("data/*/loot_tables/**")
        } else {
            exclude("data/*/loot_table/**")
        }
        if (stonecutter.eval(stonecutter.current.version, "<26.2")) {
            exclude("assets/*/items/**")
        }
    }

    // Builds the version into a shared folder in `build/libs/${mod version}/`
    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"
        inputs.property("version", project.property("mod.version"))

        from(loomx.modJar.flatMap { it.archiveFile }, loomx.modSourcesJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(requiredJava.majorVersion.toInt())
}

publishMods {
    val modrinthToken = System.getenv("MODRINTH_TOKEN")
    val curseForgeToken = System.getenv("CURSEFORGE_TOKEN")

    file = loomx.modJar.flatMap { it.archiveFile }
    dryRun = modrinthToken == null || curseForgeToken == null
    displayName = "${property("mod.name")} ${property("mod.version")} - Fabric ${minecraftVersion}"
    version = project.version.toString()
    changelog = rootProject.file("CHANGELOG.md").readText()
    type = STABLE
    modLoaders.add("fabric")
    curseforge {
        projectId = property("publish.curseforge").toString()
        accessToken = curseForgeToken ?: ""
        compatibleVersions.forEach { minecraftVersions.add(it) }
        client = true
        server = true
        requires("fabric-api")
    }
    modrinth {
        projectId = property("publish.modrinth").toString()
        accessToken = modrinthToken
        compatibleVersions.forEach { minecraftVersions.add(it) }
        environment = CLIENT_OR_SERVER
        requires("fabric-api")
    }
}
