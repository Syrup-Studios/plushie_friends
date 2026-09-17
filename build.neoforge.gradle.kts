import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    id("net.neoforged.moddev") version "2.0.147"
    id("maven-publish")
}

val minecraftVersion = property("deps.minecraft") as String
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
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    sourceCompatibility = JavaVersion.toVersion(targetJavaVersion)
    targetCompatibility = JavaVersion.toVersion(targetJavaVersion)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.named<ProcessResources>("processResources") {
    val props = mapOf(
        "version" to project.version.toString(),
        "mc" to minecraftVersion,
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
    if ((props["mc"] as String).startsWith("1.")) {
        exclude("assets/*/items/**")
    }
}

tasks.named("createMinecraftArtifacts") {
    dependsOn("stonecutterGenerate")
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.named("jar"), tasks.named("sourcesJar"))
    into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    dependsOn("build")
}
