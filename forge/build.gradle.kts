@file:Suppress("UnstableApiUsage")

architectury {
    forge()
}

loom {
    forge {
        convertAccessWideners = true
        extraAccessWideners.add( loom.accessWidenerPath.get().asFile.name)
    }
}

dependencies {
    forge (libs.forge)
    // Remove the next line if you don't want to depend on the API
    modApi (libs.architectury.forge)
}

tasks.processResources {
    inputs.property( "version", project.version)

    filesMatching("META-INF/mods.toml") {
        expand ("version" to project.version)
    }
}
