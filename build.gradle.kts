@file:Suppress("UnstableApiUsage")

import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    id("architectury-plugin") version "3.4-SNAPSHOT"
    `kotlin-dsl`
    id("dev.architectury.loom") version "1.3-SNAPSHOT" apply false
    id("io.github.juuxel.loom-vineflower") version "1.11.0" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
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
