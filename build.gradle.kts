plugins {
    `java-library`
    id("io.freefair.lombok") version "6.6.1"
    `maven-publish`
}

group = "cc.polyfrost"
version = "0.0.1-SNAPSHOT"

repositories {
    maven("https://repo.polyfrost.cc/releases")
    maven("https://repo.polyfrost.cc/snapshots")
    maven("https://maven.minecraftforge.net")
}

dependencies {
    implementation("fr.stardustenterprises", "plat4k", "1.6.3")
    implementation("cc.polyfrost", "polyio", "0.0.2")

    compileOnly("net.minecraft", "launchwrapper", "1.12")
    compileOnly("net.fabricmc", "fabric-loader", "0.11.6")
    compileOnly("org.quiltmc", "quilt-loader", "0.17.11")
    compileOnly("cpw.mods", "modlauncher", "8.1.3")
//    compileOnly("cpw.mods", "securejarhandler", "2.1.6")

    implementation("org.jetbrains", "annotations", "23.0.0")
    testImplementation("org.junit.jupiter", "junit-jupiter-engine", "5.8.1")
}

java {
    withJavadocJar()
    withSourcesJar()

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    withType<Test> {
        useJUnitPlatform()
    }
}

configure<PublishingExtension> {
    publications {
        register<MavenPublication>("default") {
            from(components["java"])

            pom {
                name.set(project.name)
                description.set(File("README.md").readLines()[2])
                url.set("https://github.com/Polyfrost/${project.name}")

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
                }

                scm {
                    connection.set("scm:git:git://github.com/Polyfrost/${project.name}.git")
                    developerConnection.set("scm:git:ssh://github.com/Polyfrost/${project.name}.git")
                    url.set("https://github.com/Polyfrost/${project.name}")
                }
            }
        }
    }

    repositories {
        maven {
            name = "releases"
            setUrl("https://repo.polyfrost.cc/releases")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
        maven {
            name = "snapshots"
            setUrl("https://repo.polyfrost.cc/snapshots")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}