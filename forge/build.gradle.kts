@file:Suppress("UnstableApiUsage")

plugins {
    id ("com.github.johnrengelman.shadow")
}

val archives_base_name: String by project

architectury {
    platformSetupLoomIde()
    forge()
}

loom {
    accessWidenerPath = project(":common").loom.accessWidenerPath

    forge {
        convertAccessWideners = true
        extraAccessWideners.add( loom.accessWidenerPath.get().asFile.name)
    }
}

configurations {
    register("common")
    register("shadowCommon") // Don't use shadow from the shadow plugin since it *excludes* files.
    compileClasspath {extendsFrom( configurations["common"])}
    runtimeClasspath {extendsFrom( configurations["common"])}
    "developmentForge" {extendsFrom( configurations["common"])}
}

dependencies {
    forge (libs.forge)
    // Remove the next line if you don't want to depend on the API
    modApi (libs.architectury.forge)

    "common"(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    "shadowCommon"(project(path = ":common", configuration = "transformProductionForge")) { isTransitive = false }
}

tasks.processResources {
    inputs.property( "version", project.version)

    filesMatching("META-INF/mods.toml") {
        expand ("version" to project.version)
    }
}
tasks {
    shadowJar {
        exclude("fabric.mod.json")
        exclude("quilt.mod.json")
        exclude("architectury.common.json")

        configurations = listOf(project.configurations["shadowCommon"])
        archiveClassifier = "dev"
    }

    remapJar {
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
        create<MavenPublication>("mavenForge") {
            artifactId = archives_base_name + "-" + project.name
            from (components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
    }
}
