@file:Suppress("UnstableApiUsage")

plugins {
    id("com.github.johnrengelman.shadow")
}

val enabled_platforms: String by rootProject
val archives_base_name: String by project
val maven_group: String by project

repositories {
    maven { url = uri("https://maven.quiltmc.org/repository/release/") }
}

architectury {
    platformSetupLoomIde()
    loader("quilt")
}

loom {
    accessWidenerPath = project(":common").loom.accessWidenerPath
}

configurations {
    register("common")
    register("shadowCommon") // Don't use shadow from the shadow plugin since it *excludes* files.
    compileClasspath { extendsFrom(configurations["common"]) }
    runtimeClasspath { extendsFrom(configurations["common"]) }
    "developmentQuilt" { extendsFrom(configurations["common"]) }
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

    "common"(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    "shadowCommon"(project(path = ":common", configuration = "transformProductionQuilt")) { isTransitive = false }
    "common"(project(path = ":fabric-like", configuration = "namedElements")) { isTransitive = false }
    "shadowCommon"(project(path = ":fabric-like", configuration = "transformProductionQuilt")) { isTransitive = false }
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

    shadowJar {
        exclude("architectury.common.json")

        configurations = listOf(project.configurations["shadowCommon"])
        archiveClassifier = "dev"
    }

    remapJar {
        injectAccessWidener = true
        inputFile = shadowJar.get().archiveFile
        dependsOn(shadowJar)
        archiveClassifier = null
    }

    jar {
        archiveClassifier = "dev-unshadowed"
    }

    sourcesJar {
        val commonSources = project(":common").tasks.sourcesJar.get()
        dependsOn(commonSources)
        from(commonSources.archiveFile.map { zipTree(it) })
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}

components.named<AdhocComponentWithVariants>("java") {
    withVariantsFromConfiguration(project.configurations["shadowRuntimeElements"]) {
        skip()
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenQuilt") {
            artifactId = archives_base_name + "-" + project.name
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
    }
}
