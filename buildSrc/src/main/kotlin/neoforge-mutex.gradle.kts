// Adapted from TeamMidnightDust/MidnightLib (https://github.com/TeamMidnightDust/MidnightLib).
// Copyright (c) 2020 MidnightDust. Licensed under the MIT License.
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

interface NeoForgeMutex : BuildService<BuildServiceParameters.None>

val mutex = gradle.sharedServices.registerIfAbsent("createMinecraftArtifactsMutex", NeoForgeMutex::class.java) {
    maxParallelUsages.set(1)
}

tasks.named { it == "createMinecraftArtifacts" }.configureEach {
    usesService(mutex)
}
