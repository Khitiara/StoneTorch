@file:Suppress("UnstableApiUsage")

import net.fabricmc.loom.api.LoomGradleExtensionAPI
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly
import dev.architectury.plugin.ArchitectPluginExtension
import me.shedaniel.unifiedpublishing.UnifiedPublishingExtension

plugins {
    `kotlin-dsl`
    alias(libs.plugins.architectury)
    alias(libs.plugins.loom) apply false
    alias(libs.plugins.vineflower) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.upload) apply false
}

val archives_base_name: String by project
val mod_version: String by project
val maven_group: String by project

architectury {
    minecraft = libs.versions.minecraft.ver.get()
}

subprojects {
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "io.github.juuxel.loom-vineflower")

    repositories {
        maven {
            name = "ParchmentMC"
            url = uri("https://maven.parchmentmc.org")
        }
    }

    configure<LoomGradleExtensionAPI> {
        silentMojangMappingsLicense()
    }
    val loom = the<LoomGradleExtensionAPI>()

    dependencies {
        "minecraft"(rootProject.project.libs.minecraft)
        "mappings"(loom.layered {
            officialMojangMappings()

            parchment("org.parchmentmc.data:parchment-${rootProject.project.libs.versions.minecraft.ver.get()}:${rootProject.project.libs.versions.parchment.get()}@zip")
        })
    }
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "architectury-plugin")
    apply(plugin = "maven-publish")

    configure<BasePluginExtension> {
        archivesName = archives_base_name
    }
    version = mod_version
    group = maven_group

    repositories {
        maven {
            name = "Fuzs Mod Resources"
            url = uri("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/")
        }
        maven {
            name = "Curse Maven"
            url = uri("https://cursemaven.com")
        }
        maven {
            name = "Terraformers"
            url = uri("https://maven.terraformersmc.com/")
        }
    }

    dependencies {
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release = 17
    }

    configure<JavaPluginExtension> {
        withSourcesJar()
    }
}

configure(listOf(project(":fabric"), project(":quilt"), project(":forge"))) {
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "me.shedaniel.unified-publishing")

    configure<ArchitectPluginExtension> {
        platformSetupLoomIde()
    }

    configure<LoomGradleExtensionAPI> {
        accessWidenerPath = project(":common").the<LoomGradleExtensionAPI>().accessWidenerPath
    }

    val loaderNameCaps = project.name.capitalizeAsciiOnly()
    configurations {
        register("common")
        register("shadowCommon") // Don't use shadow from the shadow plugin since it *excludes* files.
        compileClasspath { extendsFrom(configurations["common"]) }
        runtimeClasspath { extendsFrom(configurations["common"]) }
        register("development$loaderNameCaps") { extendsFrom(configurations["common"]) }
    }

    dependencies {
        "common"(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
        "shadowCommon"(project(path = ":common", configuration = "transformProductionFabric")) { isTransitive = false }
    }

    tasks {
        val shadowJar by named<ShadowJar>("shadowJar") {
            exclude("architectury.common.json")

            configurations = listOf(project.configurations["shadowCommon"])
            archiveClassifier = "dev"
        }

        val remapJar by named<RemapJarTask>("remapJar") {
            inputFile = shadowJar.archiveFile
            dependsOn(shadowJar)
            archiveClassifier = null
        }

        jar {
            archiveClassifier = "dev-unshadowed"
        }

        named<Jar>("sourcesJar") {
            val commonSources by project(":common").tasks.named<Jar>("sourcesJar")
            dependsOn(commonSources)
            from(commonSources.archiveFile.map { zipTree(it) })
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }

        val renameForPublish by register<Zip>("renamedJarForPublish") {
            from(remapJar.archiveFile.map { zipTree(it) })
            archiveExtension = "jar"
            metadataCharset = "UTF-8"
            destinationDirectory = base.libsDirectory
            archiveClassifier = project.name
        }

        assemble { dependsOn(renameForPublish) }
    }

    components.named<AdhocComponentWithVariants>("java") {
        withVariantsFromConfiguration(project.configurations["shadowRuntimeElements"]) {
            skip()
        }
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven${project.name}") {
                artifactId = archives_base_name + "-" + project.name
                from(components["java"])
            }
        }

        // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
        repositories {
            // Add repositories to publish to here.
        }
    }

    val deps: List<String> = when (project.name) {
        "forge" -> listOf("architectury-api")
        "fabric" -> listOf("architectury-api", "fabric-api")
        "quilt" -> listOf("architectury-api", "qsl")
        else -> listOf()
    }
    val mcVer = rootProject.libs.versions.minecraft.ver.get()

    configure<UnifiedPublishingExtension> {
        project {
            displayName = "[${loaderNameCaps} $mcVer] v${project.version}"
            releaseType = "release"
            gameVersions = listOf(mcVer)
            gameLoaders = listOf(project.name)
            mainPublication(tasks["renamedJarForPublish"])

            val curseforge_id: String by rootProject
            val curseApiKey: String? =
                (project.findProperty("CURSE_API_KEY") ?: System.getenv("CURSE_API_KEY")) as String?
            if (curseApiKey != null) {
                curseforge {
                    token = curseApiKey
                    id = curseforge_id
                    gameVersions.add("Java 17")
                    relations {
                        deps.forEach { depends(it) }
                    }
                }
            }

            val modrinth_id: String by rootProject
            val modrinthToken: String? =
                (project.findProperty("MODRINTH_TOKEN") ?: System.getenv("MODRINTH_TOKEN")) as String?
            if (modrinthToken != null) {
                modrinth {
                    token = modrinthToken
                    id = modrinth_id
                    version = "${project.version}+${project.name}"
                    relations {
                        deps.forEach { depends(it) }
                    }
                }
            }
        }
    }
}

configure(listOf(project(":fabric"), project(":quilt"))) {
    dependencies {
        "common"(project(path = ":fabric-like", configuration = "namedElements")) { isTransitive = false }
        "shadowCommon"(project(path = ":fabric-like", configuration = "transformProductionFabric")) {
            isTransitive = false
        }
    }

    tasks {
        named<RemapJarTask>("remapJar") {
            injectAccessWidener = true
        }
        named<Jar>("sourcesJar") {
            val flikeSources by project(":fabric-like").tasks.named<Jar>("sourcesJar")
            dependsOn(flikeSources)
            from(flikeSources.archiveFile.map { zipTree(it) })
        }
    }
}