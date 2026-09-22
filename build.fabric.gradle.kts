plugins {
    id("dev.kikugie.loom-back-compat")
    id("me.modmuss50.mod-publish-plugin")
    `maven-publish`
}

val minecraftVersion = stonecutter.current.version
val requiredJava: JavaVersion = when {
    stonecutter.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    stonecutter.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    else -> JavaVersion.VERSION_17
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
        val props = mapOf(
            "version" to project.property("mod.version"),
            "mc" to project.property("mod.fabric_mc_range"),

            "modName" to project.property("mod.name"),
            "modId" to project.property("mod.id"),
            "modDescription" to project.property("mod.description"),
            "authors" to project.property("mod.authors"),
            "license" to project.property("mod.license"),
            "homepage" to project.property("mod.homepage"),
            "issues" to project.property("mod.issues"),
            "sources" to project.property("mod.sources"),

            "fl" to project.property("deps.fabric_loader"),
            "fapi" to project.property("deps.fabric_api"),
            "java" to requiredJava.majorVersion,
        )

        inputs.properties(props)

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

        from(loomx.modJar.flatMap { it.archiveFile }, loomx.modSourcesJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(requiredJava.majorVersion.toInt())
}

val outputFile = loomx.modJar.flatMap { it.archiveFile }
val changelogText = providers.fileContents(rootProject.layout.projectDirectory.file("CHANGELOG.md")).asText
val curseForgeToken = providers.environmentVariable("CURSEFORGE_TOKEN")
val modrinthToken = providers.environmentVariable("MODRINTH_TOKEN")

publishMods {
    file.set(outputFile)
    dryRun = curseForgeToken.isPresent.not() || modrinthToken.isPresent.not()
    version = project.version.toString()
    displayName = "${property("mod.name")} ${property("mod.version")} - Fabric ${minecraftVersion}"
    changelog = changelogText
    type = STABLE
    modLoaders.add("fabric")
    curseforge {
        projectId = property("publish.curseforge").toString()
        accessToken = curseForgeToken
        compatibleVersions.forEach { minecraftVersions.add(it) }
        client = true
        server = true
        requires("fabric-api")
    }
    modrinth {
        projectId = property("publish.modrinth").toString()
        accessToken = modrinthToken
        compatibleVersions.forEach { minecraftVersions.add(it) }
        environment = CLIENT_AND_SERVER
        requires("fabric-api")
    }
}
