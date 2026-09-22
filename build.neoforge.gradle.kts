plugins {
    id("net.neoforged.moddev")
    id("neoforge-mutex")
    id("me.modmuss50.mod-publish-plugin")
    `maven-publish`
}

val minecraftVersion = stonecutter.current.version
val minecraftRange = property("mod.neoforge_mc_range") as String
val neoForgeVersion = property("deps.neoforge") as String
val targetJavaVersion = if (stonecutter.eval(stonecutter.current.version, ">=26.2")) 25 else 21

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
            programArgument("--nogui")
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
        languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
    sourceCompatibility = JavaVersion.toVersion(targetJavaVersion)
    targetCompatibility = JavaVersion.toVersion(targetJavaVersion)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.named<ProcessResources>("processResources") {
    val props = mapOf(
        "version" to project.property("mod.version").toString(),
        "mc" to minecraftRange,
        "neoforge" to neoForgeVersion,
        "modName" to project.property("mod.name").toString(),
        "modId" to project.property("mod.id").toString(),
        "modDescription" to project.property("mod.description").toString(),
        "authors" to project.property("mod.authors").toString(),
        "license" to project.property("mod.license").toString(),
        "homepage" to project.property("mod.homepage").toString(),
        "issues" to project.property("mod.issues").toString(),
        "sources" to project.property("mod.sources").toString()
    )
    val mixinJava = "JAVA_$targetJavaVersion"
    inputs.properties(props)
    filesMatching("META-INF/neoforge.mods.toml") { expand(props) }
    filesMatching("*.mixins.json") { expand("java" to mixinJava) }
    exclude("fabric.mod.json", "META-INF/mods.toml", "data/*/loot_tables/**")
    if (minecraftVersion.startsWith("1.")) {
        exclude("assets/*/items/**")
    }
}

tasks.named("createMinecraftArtifacts") {
    dependsOn("stonecutterGenerate")
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.named<Jar>("jar").flatMap { it.archiveFile }, tasks.named<Jar>("sourcesJar").flatMap { it.archiveFile })
    into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
}

val compatibleVersions = stonecutter.properties.rawOrNull("mod.mc_releases")
    ?.asList().orEmpty().map { it.toString() }
val outputFile = tasks.named<org.gradle.jvm.tasks.Jar>("jar").flatMap { it.archiveFile }
val changelogText = providers.fileContents(rootProject.layout.projectDirectory.file("CHANGELOG.md")).asText
val curseForgeToken = providers.environmentVariable("CURSEFORGE_TOKEN")
val modrinthToken = providers.environmentVariable("MODRINTH_TOKEN")

publishMods {
    file.set(outputFile)
    dryRun = curseForgeToken.isPresent.not() || modrinthToken.isPresent.not()
    version = project.version.toString()
    displayName = "${property("mod.name")} ${property("mod.version")} - Neoforge ${minecraftVersion}"
    changelog = changelogText
    type = STABLE
    modLoaders.add("neoforge")
    curseforge {
        projectId = property("publish.curseforge").toString()
        accessToken = curseForgeToken
        compatibleVersions.forEach { minecraftVersions.add(it) }
        client = true
        server = true
    }
    modrinth {
        projectId = property("publish.modrinth").toString()
        accessToken = modrinthToken
        compatibleVersions.forEach { minecraftVersions.add(it) }
        environment = CLIENT_AND_SERVER
    }
}
