@file:Suppress("UnstableApiUsage")

val maven_group: String by project

repositories {
    maven { url = uri("https://maven.quiltmc.org/repository/release/") }
}

architectury {
    loader("quilt")
}

dependencies {
    modImplementation(libs.quilt.loader)
    modApi(libs.quilt.fabricApi)
    // Remove the next few lines if you don't want to depend on the API
    modApi(libs.architectury.fabric) {
        // We must not pull Fabric Loader from Architectury Fabric
        exclude(group = "net.fabricmc")
        exclude(group = "net.fabricmc.fabric-api")
    }
}
tasks {
    processResources {
        inputs.property("group", maven_group)
        inputs.property("version", project.version)

        filesMatching("quilt.mod.json") {
            expand("group" to maven_group,
                    "version" to project.version)
        }
    }
}
