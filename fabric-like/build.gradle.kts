@file:Suppress("UnstableApiUsage")

val enabled_platforms: String by rootProject

architectury {
    common(enabled_platforms.split(","))
}

loom {
    accessWidenerPath = project(":common").loom.accessWidenerPath
}

dependencies {
    modImplementation(libs.fabric.loader)
    modApi (libs.fabric.api)
    // Remove the next line if you don't want to depend on the API
    modApi (libs.architectury.fabric)

//    modApi(libs.forgeConfigApiWrapper.fabric)

    compileOnly(project(path = ":common", configuration = "namedElements")) { isTransitive = false }

//    modLocalRuntime("com.terraformersmc:modmenu:5.0.2")
}
