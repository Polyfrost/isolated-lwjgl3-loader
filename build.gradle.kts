@file:Suppress("VulnerableLibrariesLocal")

plugins {
    java
    kotlin("jvm")
    val dgtVersion = "2.6.0"
    id("dev.deftu.gradle.tools") version(dgtVersion)
    id("dev.deftu.gradle.tools.publishing.maven") version(dgtVersion)
}

repositories {
    maven("https://maven.minecraftforge.net/")
    maven("https://libraries.minecraft.net/")
    maven("https://repo.polyfrost.org/releases")
    maven("https://repo.polyfrost.org/snapshots")
}

dependencies {
    implementation("org.polyfrost:polyio:0.1.0")

    compileOnly("org.lwjgl:lwjgl:3.3.3")
    compileOnly("net.minecraft:launchwrapper:1.12")
}

publishing {
    repositories {
        fun MavenArtifactRepository.setupPolyfrostRepo() {
            credentials(PasswordCredentials::class.java)

            authentication {
                create<BasicAuthentication>("basic")
            }
        }

        maven {
            name = "polyfrostReleases"
            url = uri("https://repo.polyfrost.org/releases")
            setupPolyfrostRepo()

        }

        maven {
            name = "polyfrostSnapshots"
            url = uri("https://repo.polyfrost.org/snapshots")
            setupPolyfrostRepo()
        }
    }
}
