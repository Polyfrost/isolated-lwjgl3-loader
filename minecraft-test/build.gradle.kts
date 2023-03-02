import cc.polyfrost.gradle.util.noServerRunConfigs
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.6.21"
    id("cc.polyfrost.multi-version")
    id("cc.polyfrost.defaults.repo")
    id("cc.polyfrost.defaults.java")
    id("cc.polyfrost.defaults.loom")
    id("com.github.johnrengelman.shadow")
    id("net.kyori.blossom") version "1.3.0"
    id("signing")
    java
}

val mod_name: String by project
val mod_version: String by project
val mod_id: String by project

preprocess {
    vars.put("MODERN", if (project.platform.mcMinor >= 16) 1 else 0)
}

blossom {
    replaceToken("@VER@", mod_version)
    replaceToken("@NAME@", mod_name)
    replaceToken("@ID@", mod_id)
}

version = mod_version
group = "cc.polyfrost"
base {
    archivesName.set("$mod_id-$platform")
}

loom {
    noServerRunConfigs()
}

val shade: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

sourceSets {
    main {
        output.setResourcesDir(java.classesDirectory)
    }
}

repositories {
    mavenLocal()
    maven("https://repo.polyfrost.cc/releases")
    maven("https://repo.polyfrost.cc/snapshots")
}

dependencies {
    if (project.platform.isFabric) {
        modImplementation("net.fabricmc:fabric-loader:0.14.12")
        modImplementation(
            if (platform.isLegacyFabric) {
                "net.legacyfabric.legacy-fabric-api:legacy-fabric-api:1.9.0+${platform.mcVersionStr}"
            } else {
                val fabricApiVersion = mapOf(
                    11404 to "0.4.3+build.247-1.14",
                    11502 to "0.5.1+build.294-1.15",
                    11601 to "0.14.0+build.371-1.16",
                    11603 to "0.17.1+build.394-1.16",
                    11604 to "0.25.1+build.416-1.16",
                    11700 to "0.36.0+1.17",
                    11701 to "0.37.1+1.17",
                    11800 to "0.43.1+1.18",
                    11801 to "0.43.1+1.18",
                    11802 to "0.47.9+1.18.2",
                    11900 to "0.55.3+1.19",
                    11901 to "0.58.5+1.19.1",
                    11902 to "0.68.0+1.19.2",
                    11903 to "0.68.1+1.19.3"
                )
                fun find(mcVer: Int): String {
                    return fabricApiVersion[mcVer] ?: find(mcVer - 1)
                }
                "net.fabricmc.fabric-api:fabric-api:${find(platform.mcVersion)}"
            }
        )
    }
    modImplementation("cc.polyfrost:universalcraft-$platform:246")
    runtimeOnly("org.apache.logging.log4j:log4j-core:2.14.1")

    implementation("cc.polyfrost", "lwjgl3-bootstrap", "0.0.7")
    compileOnly("org.lwjgl:lwjgl-nanovg:3.2.2")
}

tasks {
    processResources {
        inputs.property("id", mod_id)
        inputs.property("name", mod_name)
        val java = if (project.platform.mcMinor >= 18) {
            17
        } else {
            if (project.platform.mcMinor == 17) 16 else 8
        }
        val compatLevel = "JAVA_${java}"
        inputs.property("java", java)
        inputs.property("java_level", compatLevel)
        inputs.property("version", mod_version)
        inputs.property("mcVersionStr", project.platform.mcVersionStr)
        val dataMap = mutableMapOf(
            "id" to mod_id,
            "name" to mod_name,
            "java" to java,
            "java_level" to compatLevel,
            "version" to mod_version,
            "mcVersionStr" to project.platform.mcVersionStr
        )
        filesMatching(listOf("mcmod.info", "META-INF/mods.toml")) {
            expand(dataMap)
        }
        filesMatching("fabric.mod.json") {
            expand(dataMap.apply {
                put("mcVersionStr", project.platform.mcVersionStr.substringBeforeLast(".") + ".x")
            })
        }
    }

    withType(Jar::class.java) {
        if (project.platform.isFabric) {
            exclude("mcmod.info", "META-INF/mods.toml")
        } else {
            exclude("fabric.mod.json")
            if (project.platform.isLegacyForge) {
                exclude("META-INF/mods.toml")
            } else {
                exclude("mcmod.info")
            }
        }
    }

    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("dev")
        configurations = listOf(shade)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    remapJar {
        input.set(shadowJar.get().archiveFile)
        archiveClassifier.set("")
    }

    jar {
        manifest {
            attributes(
                mapOf(
                    "ModSide" to "CLIENT",
                    "ForceLoadAsMod" to true,
                )
            )
        }
        dependsOn(shadowJar)
        archiveClassifier.set("")
        enabled = false
    }
}