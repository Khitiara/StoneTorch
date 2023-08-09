@file:Suppress("UnstableApiUsage")

plugins {
    id("com.github.johnrengelman.shadow")
}

val enabled_platforms: String by rootProject
val archives_base_name: String by project

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    accessWidenerPath = project(":common").loom.accessWidenerPath
}

configurations {
    register("common")
    register("shadowCommon") // Don't use shadow from the shadow plugin since it *excludes* files.
    compileClasspath { extendsFrom(configurations["common"]) }
    runtimeClasspath { extendsFrom(configurations["common"]) }
    "developmentFabric" { extendsFrom(configurations["common"]) }
}

dependencies {
    modImplementation(libs.fabric.loader)
    modApi(libs.fabric.api)
    // Remove the next line if you don't want to depend on the API
    modApi(libs.architectury.fabric)

    "common"(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    "shadowCommon"(project(path = ":common", configuration = "transformProductionFabric")) { isTransitive = false }
    "common"(project(path = ":fabric-like", configuration = "namedElements")) { isTransitive = false }
    "shadowCommon"(project(path = ":fabric-like", configuration = "transformProductionFabric")) { isTransitive = false }
}
tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }

    shadowJar {
        exclude("architectury.common.json")

        configurations = listOf(project.configurations["shadowCommon"])
        archiveClassifier = "dev"
    }

    remapJar {
        injectAccessWidener = true
        inputFile = shadowJar.get().archiveFile
        dependsOn(shadowJar)
        archiveClassifier = null as String?
    }

    jar {
        archiveClassifier = "dev-unshadowed"
    }

    sourcesJar {
        val commonSources = project(":common").tasks.sourcesJar.get()
        dependsOn(commonSources)
        from(commonSources.archiveFile.map { zipTree(it) })
    }
}

components.named<AdhocComponentWithVariants>("java") {
    withVariantsFromConfiguration(project.configurations["shadowRuntimeElements"]) {
        skip()
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenFabric") {
            artifactId = archives_base_name + "-" + project.name
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
    }
}
