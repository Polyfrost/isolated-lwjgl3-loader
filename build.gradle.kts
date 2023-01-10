plugins {
    `java-library`
    `maven-publish`
}

group = "cc.polyfrost"
version = "0.0.1-SNAPSHOT"

repositories {
    maven("https://repo.polyfrost.cc/releases/")
}

dependencies {
    implementation("fr.stardustenterprises", "plat4k", "1.6.3")

    implementation("org.jetbrains", "annotations", "23.0.0")
    testImplementation("org.junit.jupiter", "junit-jupiter-engine", "5.8.1")
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<Test> {
    useJUnitPlatform()
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