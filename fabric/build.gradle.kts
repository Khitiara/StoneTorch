@file:Suppress("UnstableApiUsage")

architectury {
    fabric()
}

dependencies {
    modImplementation(libs.fabric.loader)
    modApi(libs.fabric.api)
    // Remove the next line if you don't want to depend on the API
    modApi(libs.architectury.fabric)
}
tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }
}
