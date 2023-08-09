@file:Suppress("UnstableApiUsage")

val enabled_platforms: String by rootProject
val archives_base_name: String by rootProject

architectury {
    common(enabled_platforms.split(","))
}

loom {
    accessWidenerPath = file("src/main/resources/stonetorch.accesswidener")
}

dependencies {
    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modImplementation(rootProject.project.libs.fabric.loader)
    // Remove the next line if you don't want to depend on the API
    modApi(rootProject.project.libs.architectury)
//    api(rootProject.project.libs.forgeConfigApiWrapper.common)
}

publishing {
    publications {
        create<MavenPublication>("mavenCommon") {
            artifactId = archives_base_name
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
    }
}
