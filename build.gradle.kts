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
    implementation("fr.stardustenterprises:plat4k:1.6.3")
    implementation("dev.deftu:filestream:0.1.0")

    compileOnly("org.lwjgl:lwjgl:3.3.3")
    compileOnly("net.minecraft:launchwrapper:1.12")
}

afterEvaluate {
    publishing {
        publications {
            named("mavenJava", MavenPublication::class) {
                pom {
                    name.set(project.name)
                    description.set(File("README.md").readLines()[2])
                    url.set("https://github.com/Deftu/${project.name}")

                    licenses {
                        license {
                            name.set("ISC License")
                            distribution.set("repo")
                        }
                    }

                    developers {
                        developer {
                            id.set("xtrm")
                            name.set("killian <oss@xtrm.me>")
                        }

                        developer {
                            id.set("Deftu")
                            name.set("Deftu")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/Deftu/${project.name}.git")
                        developerConnection.set("scm:git:ssh://github.com/Deftu/${project.name}.git")
                        url.set("https://github.com/Deftu/${project.name}")
                    }
                }
            }
        }
    }
}
