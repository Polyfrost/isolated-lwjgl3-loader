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
}

dependencies {
    implementation("dev.deftu:filestream:0.4.1")

    compileOnly("org.lwjgl:lwjgl:3.3.3")
    compileOnly("net.minecraft:launchwrapper:1.12")
}
