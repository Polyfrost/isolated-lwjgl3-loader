plugins {
    kotlin("jvm") version "1.6.21" apply false
    id("cc.polyfrost.multi-version.root")
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
}

preprocess {
    val forge10809 = createNode("1.8.9-forge", 10809, "srg")
    val fabric10809 = createNode("1.8.9-fabric", 10809, "yarn")
    val forge11605 = createNode("1.16.5-forge", 11605, "srg")
    val fabric11605 = createNode("1.16.5-fabric", 11605, "yarn")

    fabric11605.link(forge11605)
    forge11605.link(forge10809)
    fabric11605.link(fabric10809)
    fabric10809.link(forge10809)
}